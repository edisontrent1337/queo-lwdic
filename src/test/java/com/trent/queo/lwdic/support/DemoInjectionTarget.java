package com.trent.queo.lwdic.support;

import com.trent.queo.lwdic.annotations.Bean;
import com.trent.queo.lwdic.annotations.Inject;
import com.trent.queo.lwdic.annotations.Named;

@Bean
@Named(name = "demo")
public class DemoInjectionTarget {

	@Inject
	@Named(name = "a")
	public Integer valueA;


	@Inject
	@Named(name = "b")
	public Integer valueB;
}
