package com.sydigit.yzwater.module.infra.service.job;

import com.sydigit.yzwater.framework.quartz.core.scheduler.SchedulerManager;
import com.sydigit.yzwater.module.infra.dal.dataobject.job.JobDO;
import com.sydigit.yzwater.module.infra.dal.mysql.job.JobMapper;
import com.sydigit.yzwater.module.infra.enums.job.JobStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private SchedulerManager schedulerManager;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void triggerJob_shouldRegisterSchedulerJobBeforeTriggerWhenImportedBySql() throws Exception {
        JobDO job = buildJob(JobStatusEnum.STOP.getStatus());
        when(jobMapper.selectById(1L)).thenReturn(job);

        jobService.triggerJob(1L);

        InOrder inOrder = inOrder(schedulerManager);
        inOrder.verify(schedulerManager).addJob(1L, "xfhhVideoSyncJob", "", "0 0/30 * * * ?", 0, 0);
        inOrder.verify(schedulerManager).triggerJob(1L, "xfhhVideoSyncJob", "");
        inOrder.verify(schedulerManager).pauseJob("xfhhVideoSyncJob");
    }

    @Test
    void updateJobStatus_shouldRegisterSchedulerJobWhenEnableImportedJob() throws Exception {
        JobDO job = buildJob(JobStatusEnum.STOP.getStatus());
        when(jobMapper.selectById(1L)).thenReturn(job);

        jobService.updateJobStatus(1L, JobStatusEnum.NORMAL.getStatus());

        verify(jobMapper).updateById(JobDO.builder().id(1L).status(JobStatusEnum.NORMAL.getStatus()).build());
        verify(schedulerManager).addJob(1L, "xfhhVideoSyncJob", "", "0 0/30 * * * ?", 0, 0);
        verify(schedulerManager, never()).resumeJob("xfhhVideoSyncJob");
    }

    private static JobDO buildJob(Integer status) {
        return JobDO.builder()
                .id(1L)
                .name("XFHH视频同步 Job")
                .status(status)
                .handlerName("xfhhVideoSyncJob")
                .handlerParam("")
                .cronExpression("0 0/30 * * * ?")
                .retryCount(0)
                .retryInterval(0)
                .monitorTimeout(0)
                .build();
    }

}
