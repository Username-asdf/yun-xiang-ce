package com.umi.pojo;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectFolderTree implements Serializable{

	private int id;
	private String title;
	private List<SelectFolderTree> children;
}
