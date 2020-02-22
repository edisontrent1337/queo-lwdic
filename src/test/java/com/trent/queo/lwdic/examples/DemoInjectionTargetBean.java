package com.trent.queo.lwdic.examples;

import com.trent.queo.lwdic.annotations.Bean;
import com.trent.queo.lwdic.annotations.Inject;
import com.trent.queo.lwdic.annotations.Named;

@Bean
public class DemoInjectionTargetBean {

	@Inject
	@Named(name = "a")
	public Integer valueA;


	@Inject
	@Named(name = "b")
	public Integer valueB;

	@Inject
	public AbstractDemoBean abstractDemoBean;
}
