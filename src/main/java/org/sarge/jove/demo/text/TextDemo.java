package org.sarge.jove.demo.text;

import java.nio.file.Paths;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.builder.*;
import org.sarge.jove.common.TransientObject;
import org.sarge.jove.io.*;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.scene.core.RenderLoop;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TextDemo {
	@Autowired private LogicalDevice dev;
	@Autowired private RenderLoop loop;

	@Bean
	public static DataSource classpath() {
		return new ClasspathDataSource();
	}

	@Bean
	public static DataSource data() {
		return FileDataSource.home(Paths.get("workspace/Demo/Data"));
	}

	@Bean
	static CommandLineRunner runner(Desktop desktop) {
		return args -> {
			while(true) {
				desktop.poll();
			}
		};
	}

	@PreDestroy
	void destroy() {
		loop.stop();
		dev.waitIdle();
	}

	@Autowired
	void listener(Window window) {
		window.keyboard().keyboard().bind(button -> System.exit(0));
	}

	@Bean
	static DestructionAwareBeanPostProcessor destroyer() {
		return new DestructionAwareBeanPostProcessor() {
			@Override
			public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
				if(bean instanceof TransientObject obj && !obj.isDestroyed()) {
					obj.destroy();
				}
			}
		};
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws InterruptedException {
		ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
		SpringApplication.run(TextDemo.class, args);
	}
}
