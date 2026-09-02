package io.github.lijiajia3515.cairo.auth.domain.api.open.sns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证snsToken参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifySnsTokenArgs implements Serializable {
	private String token;
}
