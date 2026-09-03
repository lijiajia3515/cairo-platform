package io.github.lijiajia3515.cairo.auth.api.open.captcha;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CairoMultipartFile;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CaptchaWebToken;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.GetCaptchaResponse;
import io.github.lijiajia3515.cairo.auth.modules.captcha.code.CaptchaCode;
import io.github.lijiajia3515.cairo.auth.modules.captcha.code.CaptchaCodeService;
import io.github.lijiajia3515.cairo.auth.modules.captcha.code.CaptchaConstants;
import io.github.lijiajia3515.cairo.auth.modules.captcha.code.VerifyCaptchaCodeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.captcha.GetCaptchaArgs;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaToken;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaTokenService;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.StoreCaptchaTokenArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.api.client.file.temporary_file.TemporaryFileClientApiService;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@Component
public class CaptchaOpenApiService {
	private final CaptchaCodeService captchaCodeService;
	private final CaptchaTokenService captchaTokenService;
	private final ServerProperties serverProperties;
	private final TemporaryFileClientApiService temporaryFileClientApiService;

	public CaptchaOpenApiService(CaptchaCodeService captchaCodeService, CaptchaTokenService captchaTokenService, ServerProperties serverProperties, TemporaryFileClientApiService temporaryFileClientApiService) {
		this.captchaCodeService = captchaCodeService;
		this.captchaTokenService = captchaTokenService;
		this.serverProperties = serverProperties;
		this.temporaryFileClientApiService = temporaryFileClientApiService;
	}

	/**
	 * 获取图形验证码
	 *
	 * @param args 参数
	 * @param ip   ip
	 * @return 图形验证码响应模型
	 */
	@NewSpan
	@BizLog(
		bizId = "captcha:get_captcha_code",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
			@BizLog.Param(key = "ip", value = "#ip"),
		}
	)
	public GetCaptchaResponse getCaptchaCode(@Validated GetCaptchaArgs args, @Valid @NotNull String ip) {
		String captchaKey = CoreConstants.nextIdStr();
		Duration expireTime = Duration.ofMinutes(5);
		String captchaCode;
		String imageUrl;
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			AbstractCaptcha captcha = null;
			boolean isGif = false;
			CodeGenerator codeGenerator = CaptchaConstants.getCodeGenerator(args.getType());
			switch (args.getStyle()) {
				case LINE:
					captcha = new LineCaptcha(args.getWidth(), args.getHeight(), 4, 50);
					break;
				case CIRCLE:
					captcha = new CircleCaptcha(args.getWidth(), args.getHeight(), 4, 15);
					break;
				case SHEAR:
					captcha = new ShearCaptcha(args.getWidth(), args.getHeight(), 4, 4);
					break;
				case GIF:
					captcha = new GifCaptcha(args.getWidth(), args.getHeight(), 4);
					isGif = true;
					break;
			}
			captcha.setGenerator(codeGenerator);
			captchaCode = captcha.getCode();
			captcha.write(byteArrayOutputStream);
			byteArrayOutputStream.close();

			CairoMultipartFile file;
			String id = CoreConstants.nextIdStr();
			if (isGif) {
				file = new CairoMultipartFile(id + ".gif", id + ".gif", MediaType.IMAGE_GIF_VALUE, byteArrayOutputStream.toByteArray());
			} else {
				file = new CairoMultipartFile(id + ".png", id + ".png", MediaType.IMAGE_PNG_VALUE, byteArrayOutputStream.toByteArray());
			}
			List<String> fileListResp = temporaryFileClientApiService.uploadFile("cairo","captcha", file);
			List<String> list = Optional.ofNullable(fileListResp).orElseThrow();
			imageUrl = list.get(2);

			// 存储验证码
			captchaCodeService.storeCode(CaptchaCode.builder()
				.key(captchaKey)
				.type(args.getType())
				.code(captchaCode)
				.ip(ip)
				.ttl(expireTime)
				.build());

			// 返回
			return GetCaptchaResponse.builder()
				.captchaKey(captchaKey)
				.captchaType(args.getType())
				.captchaImageUrl(imageUrl)
				.expireTime(expireTime.toSeconds())
				.build();
		} catch (IOException e) {
			log.info("图片生成失败", e);
			throw new ConflictBusinessException("图片生成失败");
		}
	}

	/**
	 * 验证输入验证码
	 *
	 * @param args 参数
	 * @return 图形token
	 */
	@NewSpan
	@BizLog(
		bizId = "captcha:verify_captcha_code",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public CaptchaWebToken verifyCaptchaCode(@Validated(VerifyCaptchaCodeArgs.Service.class) VerifyCaptchaCodeArgs args) {
		captchaCodeService.verifyCode(args);
		CaptchaToken captchaToken = captchaTokenService.storeToken(StoreCaptchaTokenArgs.builder()
			.ip(args.getIp())
			.maxFailCount(2)
			.build());

		return CaptchaWebToken.builder()
			.captchaToken(captchaToken.getToken())
			.expireTime(captchaToken.getTtl())
			.build();
	}

	public final String newTempFilepath() {
		String filePrefix = Optional.ofNullable(serverProperties.getTomcat().getBasedir()).map(File::getPath).orElse("/tmp/");
		return filePrefix.concat("captcha/" + CoreConstants.nextIdStr());
	}


}
