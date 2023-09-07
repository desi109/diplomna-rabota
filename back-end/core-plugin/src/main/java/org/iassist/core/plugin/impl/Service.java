package org.iassist.core.plugin.impl;

import org.iassist.spring.boot.api.AppService;

public class Service implements AppService {

	@Override
	public String name() {
		return "Core Service";
	}

	@Override
	public int execute(int n1, int n2) {
		return 0;
	}

}
