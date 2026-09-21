package com.sydigit.yzwater.module.system.controller.admin.auth.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Schema(description = "管理后台 - 对外免登录（外链跳转）Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthExternalAutoLoginReqVO {

    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "operator1")
    @NotEmpty(message = "登录账号不能为空")
    @Length(min = 4, max = 16, message = "账号长度为 4-16 位")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "账号格式为数字以及字母")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @Schema(description = "登录成功后由外链服务承接的前端路径，须以 / 开头才会拼入跳转 URL，例如 /index")
    private String redirect;

    /**
     * prod=内网承接基地址，dev=外网承接基地址；不传默认 dev（与历史行为一致）
     */
    @Schema(description = "承接环境：prod=内网 warroom，dev=外网 warroom；不传默认 dev", example = "prod")
    private String env;

    @Schema(description = "返回方式：redirect=HTTP 302 跳转到目标 URL（默认）；json=HTTP 200 返回目标 URL 字符串", example = "json")
    private String mode;

    @AssertTrue(message = "env 只能为 prod（内网）或 dev（外网），可省略")
    @JsonIgnore
    public boolean isEnvValid() {
        if (env == null || env.isBlank()) {
            return true;
        }
        String e = env.trim();
        return "prod".equalsIgnoreCase(e) || "dev".equalsIgnoreCase(e);
    }
}
