package org.sarge.jove.demo.text;

import java.util.List;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

@Configuration
public class DescriptorConfiguration {
	@Autowired private LogicalDevice dev;

	private final Binding samplerBinding = new Binding.Builder()
			.binding(0)
			.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
			.stage(VkShaderStage.FRAGMENT)
			.build();

	@Bean
	public Layout layout() {
		return Layout.create(dev, List.of(samplerBinding));
	}

	@Bean
	public Pool pool() {
		return new Pool.Builder()
				.add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1)
				.add(VkDescriptorType.UNIFORM_BUFFER, 1)
				.max(1)
				.build(dev);
	}

	@Bean
	public DescriptorSet descriptor(Pool pool, Layout layout, Sampler sampler, View texture) {
		final DescriptorSet set = pool.allocate(layout).iterator().next();
		set.entry(samplerBinding).set(sampler.resource(texture));
		DescriptorSet.update(dev, List.of(set));
		return set;
	}
}
