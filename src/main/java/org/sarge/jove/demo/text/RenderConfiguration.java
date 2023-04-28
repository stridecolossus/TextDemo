package org.sarge.jove.demo.text;

import java.util.List;

import org.sarge.jove.model.Mesh;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.SecondaryBuffer;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.render.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;

@Configuration
public class RenderConfiguration {
	@Bean("pipeline.bind")
	static Command pipeline(Pipeline pipeline) {
		return pipeline.bind();
	}

	@Bean("descriptor.bind")
	static Command descriptor(DescriptorSet set, PipelineLayout layout) {
		return set.bind(layout);
	}

	@Bean("vbo.bind")
	static Command vbo(VertexBuffer vbo) {
		return vbo.bind(0);
	}

	@Bean
	static DrawCommand draw(Mesh mesh) {
		return new DrawCommand.Builder()
				.count(mesh.count())
				.build();
	}

	@Bean
	static Command.Sequence sequence(@Qualifier("graphics") Command.Pool pool, List<Command> commands, RenderPass pass) {
		// TODO - this is still all very messy

		final SecondaryBuffer buffer = pool.secondary();

		buffer.begin(pass.handle());

		for(Command cmd : commands) {
			buffer.add(cmd);
		}

		buffer.end();

		return buffer.sequence();
	}
}
