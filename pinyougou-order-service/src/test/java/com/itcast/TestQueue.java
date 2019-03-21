package com.itcast;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import util.IdWorker;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath*:spring/applicationContext-*.xml")
public class TestQueue {
	@Autowired
	private IdWorker idWorker;
	@Test
	public void testQueue() {
		System.out.println(idWorker.nextId());
	}
}