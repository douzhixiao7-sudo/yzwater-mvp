package com.sydigit.yzwater.module.iot.service.openapi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.Request;
import com.hikvision.artemis.sdk.Response;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.hikvision.artemis.sdk.constant.Constants;
import jakarta.annotation.Resource;
import com.sydigit.yzwater.module.config.video.YzVideoHkProperties;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.openapi.XfhhVideoEventMysqlDO;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhVideoCameraMysqlMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhVideoEventMysqlMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhVideoRegionMysqlMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 幸福河湖视频桥接服务实现。
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class XfhhVideoBridgeServiceImpl implements XfhhVideoBridgeService {

    private static final String ARTEMIS_PATH = "/artemis";
    private static final String CONTENT_TYPE = "application/json";
    private static final int MAX_PAGE_GUARD = 2000;
    private static final List<String> SIGN_HEADER_PREFIX_LIST = List.of("x-ca-key");
    private static final String DEBUG_REGION_API_PATH = "/api/resource/v1/regions";
    private static final String DEBUG_REGION_URL = "https://192.168.6.3/artemis/api/resource/v1/regions";
    private static final String DEBUG_REGION_BODY = "{\r\n    \"pageNo\": 1,\r\n    \"pageSize\": 50\r\n}";
    private static final String DEBUG_REGION_APP_KEY = "21632318";
    private static final String DEBUG_REGION_SIGNATURE = "bs2/9egIhvHUiEcN8yf3mPcixvbsv/oHOAnE3jseT0Q=";
    private static final String DEBUG_REGION_SIGNATURE_HEADERS = "x-ca-key,x-ca-nonce,x-ca-timestamp";

    private final ArtemisConfig artemisConfig;
    private final YzVideoHkProperties properties;
    private final XfhhVideoRegionMysqlMapper videoRegionMapper;
    private final XfhhVideoCameraMysqlMapper videoCameraMapper;
    private final XfhhVideoEventMysqlMapper videoEventMapper;

    @Resource
    private RestTemplate restTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncRegions() {
        videoRegionMapper.deleteAll();
        JSONArray jsonArray = fetchPagedList("/api/resource/v1/regions", new JSONObject());
        if (jsonArray == null) {
            return true;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            YzVideoRegionDO region = new YzVideoRegionDO();
            region.setIndexCode(item.getStr("indexCode"));
            region.setName(item.getStr("name"));
            region.setParentIndexCode(item.getStr("parentIndexCode"));
            region.setTreeCode(item.getStr("treeCode"));
            videoRegionMapper.insert(region);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncCameras() {
        videoCameraMapper.deleteAll();
        JSONArray jsonArray = fetchPagedList("/api/resource/v1/cameras", new JSONObject());
        if (jsonArray == null) {
            return true;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            if (i < 3) {
                log.info("[HikCameraGeo][XfhhVideo][raw] indexCode={}, name={}, lon={}, longitude={}, lat={}, latitude={}, keys={}",
                        item.getStr("cameraIndexCode"), item.getStr("cameraName"),
                        item.getStr("lon"), item.getStr("longitude"), item.getStr("lat"), item.getStr("latitude"),
                        item.keySet());
            }
            YzVideoCameraDO camera = new YzVideoCameraDO();
            camera.setCameraIndexCode(item.getStr("cameraIndexCode"));
            camera.setCameraName(item.getStr("cameraName"));
            camera.setRegionIndexCode(item.getStr("regionIndexCode"));
            camera.setChannelNo(parseInteger(item.getStr("channelNo")));
            camera.setCameraType(item.getStr("cameraType"));
            camera.setCameraTypeName(item.getStr("cameraTypeName"));
            camera.setLon(firstNotBlank(item.getStr("lon"), item.getStr("longitude")));
            camera.setLat(firstNotBlank(item.getStr("lat"), item.getStr("latitude")));
            camera.setOnline(item.getStr("online"));
            videoCameraMapper.insert(camera);
        }
        logHikCameraGeoDiagnostics(jsonArray);
        return true;
    }

    @Override
    public String getPreviewUrl(VideoPreviewUrlReqVO reqVO) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.set("cameraIndexCode", reqVO.getCameraIndexCode());
        jsonBody.set("streamType", reqVO.getStreamType() == null ? 0 : reqVO.getStreamType());
        jsonBody.set("protocol", StrUtil.blankToDefault(reqVO.getProtocol(), "ws"));
        jsonBody.set("transmode", reqVO.getTransmode() == null ? 1 : reqVO.getTransmode());
        jsonBody.set("expand", StrUtil.blankToDefault(reqVO.getExpand(), "transcode=0"));
        jsonBody.set("streamform", StrUtil.blankToDefault(reqVO.getStreamform(), "ps"));
        return doPost("/api/video/v2/cameras/previewURLs", jsonBody);
    }

    @Override
    public String getPlaybackUrl(VideoPlaybackUrlReqVO reqVO) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.set("cameraIndexCode", reqVO.getCameraIndexCode());
        jsonBody.set("recordLocation", reqVO.getRecordLocation() == null ? 0 : reqVO.getRecordLocation());
        jsonBody.set("protocol", StrUtil.blankToDefault(reqVO.getProtocol(), "ws"));
        jsonBody.set("transmode", reqVO.getTransmode() == null ? 1 : reqVO.getTransmode());
        jsonBody.set("beginTime", reqVO.getBeginTime());
        jsonBody.set("endTime", reqVO.getEndTime());
        jsonBody.set("uuid", StrUtil.blankToDefault(reqVO.getUuid(), ""));
        jsonBody.set("expand", StrUtil.blankToDefault(reqVO.getExpand(), "transcode=0"));
        jsonBody.set("streamform", StrUtil.blankToDefault(reqVO.getStreamform(), "ps"));
        jsonBody.set("lockType", reqVO.getLockType() == null ? 0 : reqVO.getLockType());
        return doPost("/api/video/v2/cameras/playbackURLs", jsonBody);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCameraOnline() {
        List<YzVideoCameraDO> cameras = videoCameraMapper.selectList();
        if (CollUtil.isEmpty(cameras)) {
            return;
        }
        String[] cameraIndexCodes = cameras.stream()
                .map(YzVideoCameraDO::getCameraIndexCode)
                .filter(StrUtil::isNotBlank)
                .toArray(String[]::new);
        if (cameraIndexCodes.length == 0) {
            return;
        }
        JSONObject jsonBody = new JSONObject();
        jsonBody.set("regionId", "root00000000");
        jsonBody.set("includeSubNode", "1");
        jsonBody.set("indexCodes", cameraIndexCodes);
        jsonBody.set("status", "1");
        JSONArray jsonArray = fetchPagedList("/api/nms/v1/online/camera/get", jsonBody);
        if (jsonArray == null) {
            return;
        }
        videoCameraMapper.resetAllOffline();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            videoCameraMapper.updateOnlineByIndexCode(item.getStr("indexCode"), item.getStr("online"));
        }
    }

    @Override
    public boolean controlCameras(VideoPtzControlReqVO reqVO) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.set("cameraIndexCode", reqVO.getCameraIndexCode());
        jsonBody.set("action", reqVO.getAction());
        jsonBody.set("command", reqVO.getCommand());
        jsonBody.set("speed", reqVO.getSpeed());
        jsonBody.set("presetIndex", reqVO.getPresetIndex() == null ? 0 : reqVO.getPresetIndex());
        return parseSuccessJson(doPost("/api/video/v1/ptzs/controlling", jsonBody)) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveEvent(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            XfhhVideoEventMysqlDO eventDO = new XfhhVideoEventMysqlDO();
            eventDO.setId(cn.hutool.core.lang.UUID.randomUUID().toString(true));
            eventDO.setCreateTime(LocalDateTime.now());
            eventDO.setEventRevContent(sb.toString());
            videoEventMapper.insert(eventDO);
            return true;
        } catch (Exception ex) {
            log.error("保存视频事件回调失败", ex);
            return false;
        }
    }

    @Override
    public String subscribeEvents(VideoEventSubscribeReqVO reqVO) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.set("eventTypes", resolveEventTypes(reqVO));
        jsonBody.set("eventDest", resolveEventDest(reqVO));
        jsonBody.set("subType", reqVO == null || reqVO.getSubType() == null ? 0 : reqVO.getSubType());
        jsonBody.set("eventLvl", resolveEventLevels(reqVO));
        return doPost("/api/eventService/v1/eventSubscriptionByEventTypes", jsonBody);
    }

    private long[] resolveEventTypes(VideoEventSubscribeReqVO reqVO) {
        if (reqVO != null && CollUtil.isNotEmpty(reqVO.getEventTypes())) {
            return reqVO.getEventTypes().stream().mapToLong(Long::longValue).toArray();
        }
        List<String> values = StrUtil.splitTrim(StrUtil.blankToDefault(properties.getEventTypes(), "42200211236000"), ',');
        return values.stream().mapToLong(Long::parseLong).toArray();
    }

    private int[] resolveEventLevels(VideoEventSubscribeReqVO reqVO) {
        if (reqVO != null && CollUtil.isNotEmpty(reqVO.getEventLevels())) {
            return reqVO.getEventLevels().stream().mapToInt(Integer::intValue).toArray();
        }
        List<String> values = StrUtil.splitTrim(StrUtil.blankToDefault(properties.getEventLevels(), "0"), ',');
        return values.stream().mapToInt(Integer::parseInt).toArray();
    }

    private String resolveEventDest(VideoEventSubscribeReqVO reqVO) {
        if (reqVO != null && StrUtil.isNotBlank(reqVO.getEventDest())) {
            return reqVO.getEventDest();
        }
        return StrUtil.blankToDefault(properties.getEventDest(), "http://127.0.0.1:48082/admin-api/jm-data/yz/video/eventRcv");
    }

    private JSONObject requestSuccessJson(String apiPath, JSONObject body) {
        String result = doPost(apiPath, body);
        JSONObject jsonRet = parseSuccessJson(result);
        if (jsonRet != null) {
            return jsonRet;
        }
        throw buildResponseException(apiPath, result);
    }

    private JSONObject parseSuccessJson(String result) {
        if (StrUtil.isBlank(result)) {
            return null;
        }
        try {
            JSONObject jsonRet = JSONUtil.parseObj(result);
            if ("0".equals(jsonRet.getStr("code"))) {
                return jsonRet;
            }
            return null;
        } catch (Exception ex) {
            log.error("解析海康返回失败", ex);
            return null;
        }
    }

    private IllegalStateException buildResponseException(String apiPath, String result) {
        if (StrUtil.isBlank(result)) {
            return new IllegalStateException(StrUtil.format("海康接口返回为空：{}", apiPath));
        }
        try {
            JSONObject jsonRet = JSONUtil.parseObj(result);
            String code = jsonRet.getStr("code");
            Object data = jsonRet.get("data");
            String msg = firstNotBlank(jsonRet.getStr("msg"), jsonRet.getStr("message"),
                    data == null ? null : data.toString());
            log.warn("[buildResponseException][海康返回失败][apiPath={}, code={}, msg={}, response={}]",
                    apiPath, code, msg, truncate(result));
            return new IllegalStateException(StrUtil.format("海康接口返回失败：{}，code={}，msg={}",
                    apiPath, StrUtil.blankToDefault(code, "未知"), StrUtil.blankToDefault(msg, "无")));
        } catch (Exception ex) {
            log.error("[buildResponseException][解析海康返回失败][apiPath={}, response={}]",
                    apiPath, truncate(result), ex);
            return new IllegalStateException(StrUtil.format("海康接口响应解析失败：{}", apiPath), ex);
        }
    }

    private Integer parseInteger(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private JSONArray fetchPagedList(String apiPath, JSONObject body) {
        int pageNo = 1;
        Integer total = null;
        int pageSize = resolvePageSize();
        JSONArray all = new JSONArray();
        while (pageNo <= MAX_PAGE_GUARD) {
            JSONObject requestBody = JSONUtil.parseObj(body);
            requestBody.set("pageNo", pageNo);
            requestBody.set("pageSize", pageSize);
            JSONObject jsonRet = requestSuccessJson(apiPath, requestBody);
            JSONObject data = jsonRet.getJSONObject("data");
            if (data != null && data.containsKey("total")) {
                total = parseInteger(String.valueOf(data.get("total")));
            }
            JSONArray list = data == null ? null : data.getJSONArray("list");
            if (CollUtil.isEmpty(list)) {
                break;
            }
            if (apiPath.contains("cameras") && pageNo == 1) {
                log.info("[HikCameraGeo][XfhhVideo][page1-first-item] {}", list.getJSONObject(0));
            }
            all.addAll(list);
            if (total != null && all.size() >= total) {
                break;
            }
            if (total == null && list.size() < pageSize) {
                break;
            }
            pageNo++;
        }
        if (pageNo > MAX_PAGE_GUARD) {
            log.warn("[fetchPagedList][分页超过保护阈值][apiPath={}, maxPage={}]", apiPath, MAX_PAGE_GUARD);
        }
        return all;
    }

    private String doPost(String apiPath, JSONObject body) {
        ensureHkConfigured();
        Map<String, String> path = new HashMap<>(2);
        path.put("https://", ARTEMIS_PATH + apiPath);
        try {
            if (DEBUG_REGION_API_PATH.equals(apiPath)) {
                return doPostRegionByOkHttpForDebug(apiPath);
            }
            log.info("[doPost][Artemis请求关键字段][method=POST_STRING, schema=https://, host={}, path={}, fullUrl={}, appKey={}, appSecret={}, contentType={}, body={}]",
                    artemisConfig.getHost(), path.get("https://"), "https://" + artemisConfig.getHost() + path.get("https://"),
                    artemisConfig.getAppKey(), artemisConfig.getAppSecret(), CONTENT_TYPE, body);
            String result = doPostStringArtemisCompat(apiPath, path, body.toString());
            if (StrUtil.isBlank(result)) {
                throw new IllegalStateException(StrUtil.format("海康接口返回为空：{}", apiPath));
            }
            log.info("[doPost][Artemis响应结果][apiPath={}, result={}]", apiPath, truncate(result));
            return result;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[doPost][调用海康接口失败][apiPath={}, body={}]", apiPath, body, ex);
            throw new IllegalStateException(StrUtil.format("调用海康接口失败：{}，原因：{}",
                    apiPath, StrUtil.blankToDefault(ex.getMessage(), ex.getClass().getSimpleName())), ex);
        }
    }

    private String doPostRegionByOkHttpForDebug(String apiPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.add("Content-Type", CONTENT_TYPE);
        headers.add("x-ca-key", DEBUG_REGION_APP_KEY);
        headers.add("x-ca-signature", DEBUG_REGION_SIGNATURE);
        headers.add("x-ca-signature-headers", DEBUG_REGION_SIGNATURE_HEADERS);
        HttpEntity<String> requestEntity = new HttpEntity<>(DEBUG_REGION_BODY, headers);
        log.info("[doPostRegionByOkHttpForDebug][开始写死请求][apiPath={}, url={}, body={}, headers={}]",
                apiPath, DEBUG_REGION_URL, DEBUG_REGION_BODY, headers);
        ResponseEntity<String> response = buildUnsafeRestTemplateForDebug()
                .exchange(DEBUG_REGION_URL, HttpMethod.POST, requestEntity, String.class);
        String result = response.getBody();
        log.info("[doPostRegionByOkHttpForDebug][写死请求返回][apiPath={}, statusCode={}, headers={}, body={}]",
                apiPath, response.getStatusCode(), response.getHeaders(), truncate(result));
        return result;
    }

    private RestTemplate buildUnsafeRestTemplateForDebug() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllManagers = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            sslContext.init(null, trustAllManagers, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(socketFactory);
                        httpsConnection.setHostnameVerifier(hostnameVerifier);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };
            return new RestTemplate(requestFactory);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("构建忽略SSL校验的调试RestTemplate失败", ex);
        }
    }

    private void ensureHkConfigured() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("海康视频能力未启用，请检查配置 yz.video.hk.enabled");
        }
        if (StrUtil.hasBlank(artemisConfig.getHost(), artemisConfig.getAppKey(), artemisConfig.getAppSecret())) {
            throw new IllegalStateException("海康参数未配置完整，请检查配置 yz.video.hk.host/app-key/app-secret");
        }
    }

    private int resolvePageSize() {
        Integer pageSize = properties.getPageSize();
        if (pageSize == null || pageSize <= 0) {
            return 500;
        }
        return Math.min(pageSize, 1000);
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String truncate(String value) {
        return StrUtil.maxLength(value, 1000);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String doPostStringArtemisCompat(String apiPath, Map<String, String> path, String body) throws Exception {
        Request<String> request = buildPostStringRequest(path, body);
        logArtemisSdkRequest(apiPath, request);
        try {
            Response response = executeArtemisRequest(request);
            return getResponseBody(response);
        } catch (NoSuchMethodException ex) {
            return invokeLegacyArtemisMethod(path, body);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof Exception targetEx) {
                throw targetEx;
            }
            throw new RuntimeException(cause);
        }
    }

    private void logArtemisSdkRequest(String apiPath, Request<String> request) {
        ArtemisSignDebugInfo signDebugInfo = buildArtemisSignDebugInfo(request);
        log.info("[doPostStringArtemisCompat][Artemis SDK请求前Request][apiPath={}, method={}, host={}, path={}, fullUrl={}, appKey={}, appSecret={}, timeout={}, accept={}, contentType={}, headers={}, querys={}, stringBody={}, signHeaderPrefixList={}, x-ca-key={}, x-ca-nonce={}, x-ca-timestamp={}, x-ca-signature-headers={}, x-ca-signature={}, stringToSign={}]",
                apiPath, request.getMethod(), request.getHost(), request.getPath(), request.getHost() + request.getPath(),
                request.getAppKey(), request.getAppSecret(), request.getTimeout(), request.getHeaders().get("Accept"),
                request.getHeaders().get("Content-Type"), request.getHeaders(),
                request.getQuerys(), request.getStringBody(), request.getSignHeaderPrefixList(),
                signDebugInfo.xCaKey, signDebugInfo.xCaNonce, signDebugInfo.xCaTimestamp,
                signDebugInfo.xCaSignatureHeaders, signDebugInfo.xCaSignature, signDebugInfo.stringToSign);
    }

    private Request<String> buildPostStringRequest(Map<String, String> path, String body) {
        String schema = path.keySet().stream().findFirst().orElse(null);
        if (StrUtil.isBlank(schema)) {
            throw new IllegalStateException("Artemis请求路径协议缺失");
        }
        Map<String, String> headers = new HashMap<>(2);
        headers.put("Accept", "*/*");
        headers.put("Content-Type", CONTENT_TYPE);
        Request<String> request = new Request<>(com.hikvision.artemis.sdk.enums.Method.POST_STRING,
                schema + artemisConfig.getHost(), path.get(schema), artemisConfig.getAppKey(),
                artemisConfig.getAppSecret(), Constants.DEFAULT_TIMEOUT);
        request.setHeaders(headers);
        request.setQuerys(null);
        request.setStringBody(body);
        request.setSignHeaderPrefixList(new ArrayList<>(SIGN_HEADER_PREFIX_LIST));
        return request;
    }

    protected Response executeArtemisRequest(Request<String> request) throws Exception {
        Method executeMethod = ArtemisHttpUtil.class.getDeclaredMethod("execute", Request.class);
        executeMethod.setAccessible(true);
        return (Response) executeMethod.invoke(null, request);
    }

    private String getResponseBody(Response response) {
        String responseBody = response.getBody();
        int statusCode = response.getStatusCode();
        String statusPrefix = String.valueOf(statusCode);
        if (statusPrefix.startsWith("2") || statusPrefix.startsWith("3")) {
            return responseBody;
        }
        return responseBody;
    }

    private ArtemisSignDebugInfo buildArtemisSignDebugInfo(Request<String> request) {
        try {
            Map<String, String> headers = new HashMap<>(request.getHeaders());
            headers.put("x-ca-timestamp", String.valueOf(new Date().getTime()));
            headers.put("x-ca-nonce", UUID.randomUUID().toString());
            headers.put("x-ca-key", request.getAppKey());
            List<String> signHeaderPrefixList = request.getSignHeaderPrefixList() == null
                    ? null : new ArrayList<>(request.getSignHeaderPrefixList());
            Map<String, String> headersForStringToSign = new HashMap<>(headers);
            List<String> signHeadersForStringToSign = signHeaderPrefixList == null
                    ? null : new ArrayList<>(signHeaderPrefixList);
            String httpMethod = "POST";
            String stringToSign = buildStringToSign(httpMethod, request.getPath(),
                    headersForStringToSign, request.getQuerys(), null, signHeadersForStringToSign);
            Map<String, String> headersForSignature = new HashMap<>(headers);
            List<String> signHeadersForSignature = signHeaderPrefixList == null
                    ? null : new ArrayList<>(signHeaderPrefixList);
            String signature = com.hikvision.artemis.sdk.util.SignUtil.sign(request.getAppSecret(),
                    httpMethod, request.getPath(), headersForSignature, request.getQuerys(),
                    null, signHeadersForSignature);
            return new ArtemisSignDebugInfo(headers.get("x-ca-key"), headers.get("x-ca-nonce"),
                    headers.get("x-ca-timestamp"),
                    headersForStringToSign.get("x-ca-signature-headers"),
                    signature, stringToSign.replace("\n", "\\n"));
        } catch (Exception ex) {
            throw new IllegalStateException("构建Artemis签名调试信息失败", ex);
        }
    }

    private String buildStringToSign(String method, String path, Map<String, String> headers,
                                     Map<String, Object> querys, Map<String, String> bodys,
                                     List<String> signHeaderPrefixList) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(method.toUpperCase()).append('\n');
        appendHeaderValue(builder, headers, "Accept");
        appendHeaderValue(builder, headers, "Content-MD5");
        appendContentType(builder, headers.get("Content-Type"));
        appendHeaderValue(builder, headers, "Date");
        builder.append(buildHeaders(headers, signHeaderPrefixList));
        builder.append(buildResource(path, querys, bodys));
        return builder.toString();
    }

    private void appendHeaderValue(StringBuilder builder, Map<String, String> headers, String key) {
        if (headers != null && headers.get(key) != null) {
            builder.append(headers.get(key));
        }
        builder.append('\n');
    }

    private void appendContentType(StringBuilder builder, String contentType) {
        if (contentType != null) {
            if (contentType.contains("boundary")) {
                String[] parts = contentType.split(";");
                for (String part : parts) {
                    if (part.contains("boundary")) {
                        continue;
                    }
                    builder.append(part).append(';');
                }
                if (builder.charAt(builder.length() - 1) == ';') {
                    builder.deleteCharAt(builder.length() - 1);
                }
            } else {
                builder.append(contentType);
            }
        }
        builder.append('\n');
    }

    private String buildHeaders(Map<String, String> headers, List<String> signHeaderPrefixList) {
        StringBuilder headersToSign = new StringBuilder();
        if (signHeaderPrefixList != null) {
            signHeaderPrefixList.remove("x-ca-signature");
            signHeaderPrefixList.remove("Accept");
            signHeaderPrefixList.remove("Content-MD5");
            signHeaderPrefixList.remove("Content-Type");
            signHeaderPrefixList.remove("Date");
            Collections.sort(signHeaderPrefixList);
        }
        if (headers == null) {
            return headersToSign.toString();
        }
        TreeMap<String, String> treeMap = new TreeMap<>(headers);
        StringBuilder signatureHeaders = new StringBuilder();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            if (!isHeaderToSign(entry.getKey(), signHeaderPrefixList)) {
                continue;
            }
            headersToSign.append(entry.getKey()).append(':');
            if (StrUtil.isNotBlank(entry.getValue())) {
                headersToSign.append(entry.getValue());
            }
            headersToSign.append('\n');
            if (signatureHeaders.length() > 0) {
                signatureHeaders.append(',');
            }
            signatureHeaders.append(entry.getKey());
        }
        headers.put("x-ca-signature-headers", signatureHeaders.toString());
        return headersToSign.toString();
    }

    private boolean isHeaderToSign(String headerName, List<String> signHeaderPrefixList) {
        if (StrUtil.isBlank(headerName) || "x-ca-path".equals(headerName)) {
            return false;
        }
        if (headerName.startsWith("x-ca-")) {
            return true;
        }
        if (signHeaderPrefixList == null) {
            return false;
        }
        for (String prefix : signHeaderPrefixList) {
            if (headerName.equalsIgnoreCase(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String buildResource(String path, Map<String, Object> querys, Map<String, String> bodys) {
        StringBuilder resource = new StringBuilder();
        if (StrUtil.isNotBlank(path)) {
            resource.append(path);
        }
        TreeMap<String, Object> params = new TreeMap<>();
        if (querys != null) {
            params.putAll(querys);
        }
        if (bodys != null) {
            params.putAll(bodys);
        }
        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (StrUtil.isBlank(entry.getKey())) {
                continue;
            }
            if (queryBuilder.length() > 0) {
                queryBuilder.append('&');
            }
            queryBuilder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        if (queryBuilder.length() > 0) {
            resource.append('?').append(queryBuilder);
        }
        return resource.toString();
    }

    private static final class ArtemisSignDebugInfo {
        private final String xCaKey;
        private final String xCaNonce;
        private final String xCaTimestamp;
        private final String xCaSignatureHeaders;
        private final String xCaSignature;
        private final String stringToSign;

        private ArtemisSignDebugInfo(String xCaKey, String xCaNonce, String xCaTimestamp,
                                     String xCaSignatureHeaders, String xCaSignature,
                                     String stringToSign) {
            this.xCaKey = xCaKey;
            this.xCaNonce = xCaNonce;
            this.xCaTimestamp = xCaTimestamp;
            this.xCaSignatureHeaders = xCaSignatureHeaders;
            this.xCaSignature = xCaSignature;
            this.stringToSign = stringToSign;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String invokeLegacyArtemisMethod(Map<String, String> path, String body) throws Exception {
        try {
            Method legacyMethod6 = ArtemisHttpUtil.class.getMethod("doPostStringArtemis",
                    Map.class, String.class, Map.class, String.class, String.class, Map.class);
            return (String) legacyMethod6.invoke(null, path, body, null, CONTENT_TYPE, null, null);
        } catch (NoSuchMethodException ex) {
            Method legacyMethod5 = ArtemisHttpUtil.class.getMethod("doPostStringArtemis",
                    Map.class, String.class, Map.class, String.class, String.class);
            return (String) legacyMethod5.invoke(null, path, body, null, CONTENT_TYPE, null);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof Exception targetEx) {
                throw targetEx;
            }
            throw new RuntimeException(cause);
        }
    }

    /**
     * 仅诊断海康 /api/resource/v1/cameras 是否返回经纬度，不参与业务逻辑。
     */
    private void logHikCameraGeoDiagnostics(JSONArray items) {
        if (items == null || items.isEmpty()) {
            log.info("[HikCameraGeo][XfhhVideo][summary] total=0, withLon=0, withLat=0, api=/api/resource/v1/cameras, "
                    + "ds=xfhhMysql");
            return;
        }
        long withLon = 0;
        long withLat = 0;
        JSONObject sampleWithGeo = null;
        JSONObject sampleWithoutGeo = null;
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String resolvedLon = firstNotBlank(item.getStr("lon"), item.getStr("longitude"));
            String resolvedLat = firstNotBlank(item.getStr("lat"), item.getStr("latitude"));
            if (StrUtil.isNotBlank(resolvedLon)) {
                withLon++;
                if (sampleWithGeo == null) {
                    sampleWithGeo = item;
                }
            } else if (sampleWithoutGeo == null) {
                sampleWithoutGeo = item;
            }
            if (StrUtil.isNotBlank(resolvedLat)) {
                withLat++;
            }
        }
        log.info("[HikCameraGeo][XfhhVideo][summary] total={}, withLon={}, withLat={}, api=/api/resource/v1/cameras, "
                        + "ds=xfhhMysql",
                items.size(), withLon, withLat);
        if (sampleWithGeo != null) {
            log.info("[HikCameraGeo][XfhhVideo][sample-with-geo] indexCode={}, lon={}, longitude={}, lat={}, latitude={}",
                    sampleWithGeo.getStr("cameraIndexCode"), sampleWithGeo.getStr("lon"),
                    sampleWithGeo.getStr("longitude"), sampleWithGeo.getStr("lat"), sampleWithGeo.getStr("latitude"));
        }
        if (sampleWithoutGeo != null) {
            log.info("[HikCameraGeo][XfhhVideo][sample-without-geo] indexCode={}, name={}, lon={}, longitude={}, lat={}, "
                            + "latitude={}",
                    sampleWithoutGeo.getStr("cameraIndexCode"), sampleWithoutGeo.getStr("cameraName"),
                    sampleWithoutGeo.getStr("lon"), sampleWithoutGeo.getStr("longitude"),
                    sampleWithoutGeo.getStr("lat"), sampleWithoutGeo.getStr("latitude"));
        }
    }
}
