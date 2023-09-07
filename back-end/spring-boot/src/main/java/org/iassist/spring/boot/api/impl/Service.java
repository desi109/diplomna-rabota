package org.iassist.spring.boot.api.impl;

import org.iassist.spring.boot.api.AppService;

public class Service implements AppService {

	@Override
	public int execute(int n1, int n2) {
		return 1;
	}

	@Override
	public String name() {
		return "Get Data Service";
	}

}
