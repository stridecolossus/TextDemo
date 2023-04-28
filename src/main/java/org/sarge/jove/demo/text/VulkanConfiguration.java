package org.sarge.jove.demo.text;

import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.Allocator;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.springframework.context.annotation.*;

@Configuration
class VulkanConfiguration {
	@Bean
	static VulkanLibrary library() {
		return VulkanLibrary.create();
	}

	@Bean
	static Instance instance(VulkanLibrary lib, Desktop desktop) {
		return new Instance.Builder()
				.name("TextDemo")
				.extension(Handler.EXTENSION)
				.extensions(desktop.extensions())
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(lib);
	}

	@Bean
	static Handler diagnostics(Instance instance) {
		return new Handler.Builder().build(instance);
	}

	@Bean
	static Allocator allocator(LogicalDevice dev) {
		return Allocator.create(dev);
	}
}
