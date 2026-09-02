package io.github.lijiajia3515.cairo.core.page;

import lombok.Builder;

public interface PageArgs {
	default int getPage(){
		return 0;
	};

	default int getSize(){
		return 10;
	};
}
