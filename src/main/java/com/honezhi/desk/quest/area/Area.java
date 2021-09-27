package com.honezhi.desk.quest.area;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class Area {

	private int prefix;
	private short code;
	private String name;
	
	public Area(int prefix, short code, String name) {
		super();
		this.prefix = prefix;
		this.code = code;
		this.name = name;
	}

}
