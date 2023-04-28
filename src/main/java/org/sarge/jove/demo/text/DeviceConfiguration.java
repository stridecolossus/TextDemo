package org.sarge.jove.demo.text;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.springframework.context.annotation.*;

@Configuration
class DeviceConfiguration {
	private final Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);
	private final Selector presentation;

	public DeviceConfiguration(Handle surface) {
		presentation = Selector.of(surface);
	}

	@Bean
	public PhysicalDevice physical(Instance instance) {
		return PhysicalDevice.devices(instance)
				.filter(graphics)
				.filter(presentation)
				.findAny()
				.orElseThrow(() -> new RuntimeException("No suitable physical device available"));
	}

	@Bean
	public LogicalDevice device(PhysicalDevice dev) {
		return new LogicalDevice.Builder(dev)
				.extension(Swapchain.EXTENSION)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.queue(new RequiredQueue(graphics.select(dev)))
				.queue(new RequiredQueue(presentation.select(dev)))
				.feature("samplerAnisotropy")
				.build();
	}

	private static Command.Pool pool(LogicalDevice dev, Selector selector) {
		final WorkQueue.Family family = selector.select(dev.parent());
		final WorkQueue queue = dev.queue(family);
		return Command.Pool.create(dev, queue);
	}

	@Bean
	public Command.Pool graphics(LogicalDevice dev) {
		return pool(dev, graphics);
	}

	@Bean
	public Command.Pool presentation(LogicalDevice dev) {
		return pool(dev, presentation);
	}
}
