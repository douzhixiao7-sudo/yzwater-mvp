package com.sydigit.yzwater.module.infra.service.job;

import cn.hutool.extra.spring.SpringUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.framework.quartz.core.scheduler.SchedulerManager;
import com.sydigit.yzwater.framework.quartz.core.util.CronUtils;
import com.sydigit.yzwater.module.infra.controller.admin.job.vo.job.JobPageReqVO;
import com.sydigit.yzwater.module.infra.controller.admin.job.vo.job.JobSaveReqVO;
import com.sydigit.yzwater.module.infra.dal.dataobject.job.JobDO;
import com.sydigit.yzwater.module.infra.dal.mysql.job.JobMapper;
import com.sydigit.yzwater.module.infra.enums.job.JobStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.containsAny;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_CHANGE_STATUS_EQUALS;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_CHANGE_STATUS_INVALID;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_CRON_EXPRESSION_VALID;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_HANDLER_BEAN_NOT_EXISTS;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_HANDLER_BEAN_TYPE_ERROR;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_HANDLER_EXISTS;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_NOT_EXISTS;
import static com.sydigit.yzwater.module.infra.enums.ErrorCodeConstants.JOB_UPDATE_ONLY_NORMAL_STATUS;

/**
 * 定时任务 Service 实现类
 */
@Service
@Validated
@Slf4j
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(JobSaveReqVO createReqVO) throws SchedulerException {
        validateCronExpression(createReqVO.getCronExpression());
        if (jobMapper.selectByHandlerName(createReqVO.getHandlerName()) != null) {
            throw exception(JOB_HANDLER_EXISTS);
        }
        validateJobHandlerExists(createReqVO.getHandlerName());

        JobDO job = BeanUtils.toBean(createReqVO, JobDO.class);
        job.setStatus(JobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);

        schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                createReqVO.getRetryCount(), createReqVO.getRetryInterval());
        JobDO updateObj = JobDO.builder().id(job.getId()).status(JobStatusEnum.NORMAL.getStatus()).build();
        jobMapper.updateById(updateObj);
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(JobSaveReqVO updateReqVO) throws SchedulerException {
        validateCronExpression(updateReqVO.getCronExpression());
        JobDO job = validateJobExists(updateReqVO.getId());
        if (!job.getStatus().equals(JobStatusEnum.NORMAL.getStatus())) {
            throw exception(JOB_UPDATE_ONLY_NORMAL_STATUS);
        }
        validateJobHandlerExists(updateReqVO.getHandlerName());

        JobDO updateObj = BeanUtils.toBean(updateReqVO, JobDO.class);
        fillJobMonitorTimeoutEmpty(updateObj);
        jobMapper.updateById(updateObj);

        schedulerManager.updateJob(job.getHandlerName(), updateReqVO.getHandlerParam(), updateReqVO.getCronExpression(),
                updateReqVO.getRetryCount(), updateReqVO.getRetryInterval());
    }

    private void validateJobHandlerExists(String handlerName) {
        try {
            Object handler = SpringUtil.getBean(handlerName);
            assert handler != null;
            if (!(handler instanceof JobHandler)) {
                throw exception(JOB_HANDLER_BEAN_TYPE_ERROR);
            }
        } catch (NoSuchBeanDefinitionException e) {
            throw exception(JOB_HANDLER_BEAN_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobStatus(Long id, Integer status) throws SchedulerException {
        if (!containsAny(status, JobStatusEnum.NORMAL.getStatus(), JobStatusEnum.STOP.getStatus())) {
            throw exception(JOB_CHANGE_STATUS_INVALID);
        }
        JobDO job = validateJobExists(id);
        if (job.getStatus().equals(status)) {
            throw exception(JOB_CHANGE_STATUS_EQUALS);
        }

        JobDO updateObj = JobDO.builder().id(id).status(status).build();
        jobMapper.updateById(updateObj);

        boolean schedulerJobCreated = ensureSchedulerJobExists(job);
        if (JobStatusEnum.NORMAL.getStatus().equals(status)) {
            if (!schedulerJobCreated) {
                schedulerManager.resumeJob(job.getHandlerName());
            }
        } else {
            schedulerManager.pauseJob(job.getHandlerName());
        }
    }

    @Override
    public void triggerJob(Long id) throws SchedulerException {
        JobDO job = validateJobExists(id);

        boolean schedulerJobCreated = ensureSchedulerJobExists(job);
        schedulerManager.triggerJob(job.getId(), job.getHandlerName(), job.getHandlerParam());
        // SQL 直接导入的暂停任务补注册后默认会处于启用态，这里手动触发后恢复暂停状态。
        if (schedulerJobCreated && Objects.equals(job.getStatus(), JobStatusEnum.STOP.getStatus())) {
            schedulerManager.pauseJob(job.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncJob() throws SchedulerException {
        List<JobDO> jobList = jobMapper.selectList();
        for (JobDO job : jobList) {
            schedulerManager.deleteJob(job.getHandlerName());
            schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                    job.getRetryCount(), job.getRetryInterval());
            if (Objects.equals(job.getStatus(), JobStatusEnum.STOP.getStatus())) {
                schedulerManager.pauseJob(job.getHandlerName());
            }
            log.info("[syncJob][id({}) handlerName({}) 同步完成]", job.getId(), job.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) throws SchedulerException {
        JobDO job = validateJobExists(id);
        jobMapper.deleteById(id);
        schedulerManager.deleteJob(job.getHandlerName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobList(List<Long> ids) throws SchedulerException {
        List<JobDO> jobs = jobMapper.selectByIds(ids);
        jobMapper.deleteByIds(ids);
        for (JobDO job : jobs) {
            schedulerManager.deleteJob(job.getHandlerName());
        }
    }

    private JobDO validateJobExists(Long id) {
        JobDO job = jobMapper.selectById(id);
        if (job == null) {
            throw exception(JOB_NOT_EXISTS);
        }
        return job;
    }

    private void validateCronExpression(String cronExpression) {
        if (!CronUtils.isValid(cronExpression)) {
            throw exception(JOB_CRON_EXPRESSION_VALID);
        }
    }

    private boolean ensureSchedulerJobExists(JobDO job) throws SchedulerException {
        try {
            schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(),
                    job.getCronExpression(), job.getRetryCount(), job.getRetryInterval());
            log.warn("[ensureSchedulerJobExists][handlerName({}) 不存在于 Quartz，已根据 infra_job 自动补注册]",
                    job.getHandlerName());
            return true;
        } catch (ObjectAlreadyExistsException ignored) {
            return false;
        }
    }

    @Override
    public JobDO getJob(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public PageResult<JobDO> getJobPage(JobPageReqVO pageReqVO) {
        return jobMapper.selectPage(pageReqVO);
    }

    private static void fillJobMonitorTimeoutEmpty(JobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

}
