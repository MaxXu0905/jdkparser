package com.ailk.utils;import java.io.File;import java.util.ArrayList;import java.util.List;import org.htmlparser.Node;import org.htmlparser.NodeFilter;import org.htmlparser.Parser;import org.htmlparser.util.SimpleNodeIterator;public class JDKParser {	public static String BR = "\n";	public static Clazz parseDoc(String htmlPath) throws Exception {		final Clazz clazz = new Clazz();		Parser parser = new Parser(htmlPath);		parser.setEncoding("utf8");		parser.extractAllNodesThatMatch(new NodeFilter() {			@Override			public boolean accept(Node node) {				extractInfo(clazz, node);				return true;			}		});		return clazz;	}	public static void extractInfo(final Clazz clazz, Node node) {				// 获取类名		if ("div class=\"header\"".equals(node.getText())) {			SimpleNodeIterator iter = node.getChildren().elements();			StringBuilder name = new StringBuilder();			while (iter.hasMoreNodes()) {				Node _node = iter.nextNode();				String partName = _node.toPlainTextString();				if (!BR.equals(partName)) {					partName = partName.replace("Class ", ".");					partName = partName.replace("Interface ", ".");					name.append(partName);				}			}			clazz.setName(name.toString());		}		if ("div class=\"details\"".equals(node.getText())) {			Node detailWrapperNode = getFirstNotBRChild(getFirstNotBRChild(node));			if (detailWrapperNode != null) {				SimpleNodeIterator iter = detailWrapperNode.getChildren().elements();				while (iter.hasMoreNodes()) {					Node _node = iter.nextNode();					if ("ul class=\"blockList\"".equals(_node.getText())) {						_node = getFirstNotBRChild(_node);						SimpleNodeIterator iter2 = _node.getChildren().elements();						/**						 * <p>						 * parser顺序解析						 * </p>						 * <p>						 * 1 根据h3标签判定是Field，Constructor或Method						 * </p>						 * <p>						 * 2 根据类型解析						 * </p>						 */						String currType = "";						while (iter2.hasMoreNodes()) {							Node _node2 = iter2.nextNode();							// 1 获取类型							if ("h3".equals(_node2.getText())) {								currType = _node2.toPlainTextString().split(" ")[0];							} else if (("ul class=\"blockList\"".equals(_node2.getText()))									|| ("ul class=\"blockListLast\"".equals(_node2.getText()))) {								// 2 解析								extractDetailByType(getFirstNotBRChild(_node2), currType, clazz);							}						}					}				}			}		}	}	private static void extractDetailByType(Node node, String currType, final Clazz clazz) {		if ("Field".equals(currType)) {			if (clazz.getFields() == null) {				clazz.setFields(new ArrayList<Field>());			}			extractDetail4Field(node, clazz);		} else if ("Constructor".equals(currType)) {			if (clazz.getConstructors() == null) {				clazz.setConstructors(new ArrayList<Constructor>());			}			extractDetail4Constructor(node, clazz);		} else if ("Method".equals(currType)) {			if (clazz.getMethods() == null) {				clazz.setMethods(new ArrayList<Method>());			}			extractDetail4Method(node, clazz);		}	}	private static void extractDetail4Method(Node node, Clazz clazz) {		Method method = new Method();		SimpleNodeIterator iter = node.getChildren().elements();		StringBuilder signatureBuilder = new StringBuilder();		StringBuilder desBuilder = new StringBuilder();		boolean preLoading = false;		while (iter.hasMoreNodes()) {			Node _node = iter.nextNode();			String nodeText = _node.getText();			if ("h4".equals(nodeText)) {				method.setName(_node.toPlainTextString());			}			// pre内部的元素会被parser认作为siblings，同时</pre>也会被parser认作为一个元素			if ("pre".equals(nodeText)) { // pre开始				preLoading = true;			}			if ("/pre".equals(nodeText)) { // pre 结束				preLoading = false;			}			if (preLoading) {				String signPart = _node.getNextSibling().toPlainTextString();				if (!BR.equals(signPart)) {					signatureBuilder.append(signPart);				}			}			if ("div class=\"block\"".equals(nodeText)) {				desBuilder.append(_node.toPlainTextString());			}			if ("dl".equals(nodeText)) {				desBuilder.append(_node.toHtml().replaceAll("<a href=\".*?>", "<span href=\"#\">"));			}		}		method.setSignature(signatureBuilder.toString());		method.setDescription(desBuilder.toString());		clazz.getMethods().add(method);	}	private static void extractDetail4Constructor(Node node, Clazz clazz) {		Constructor constructor = new Constructor();		SimpleNodeIterator iter = node.getChildren().elements();		StringBuilder signatureBuilder = new StringBuilder();		StringBuilder desBuilder = new StringBuilder();		boolean preLoading = false;		while (iter.hasMoreNodes()) {			Node _node = iter.nextNode();			String nodeText = _node.getText();			if ("h4".equals(nodeText)) {				constructor.setName(_node.toPlainTextString());			}			// pre内部的元素会被parser认作为siblings，同时</pre>也会被parser认作为一个元素			if ("pre".equals(nodeText)) { // pre开始				preLoading = true;			}			if ("/pre".equals(nodeText)) { // pre 结束				preLoading = false;			}			if (preLoading) {				String signPart = _node.getNextSibling().toPlainTextString();				if (!BR.equals(signPart)) {					signatureBuilder.append(signPart);				}			}			if ("div class=\"block\"".equals(nodeText)) {				desBuilder.append(_node.toPlainTextString());			}			if ("dl".equals(nodeText)) {				desBuilder.append(_node.toHtml());			}		}		constructor.setSignature(signatureBuilder.toString());		constructor.setDescription(desBuilder.toString());		clazz.getConstructors().add(constructor);	}	private static void extractDetail4Field(Node node, final Clazz clazz) {		Field field = new Field();		SimpleNodeIterator iter = node.getChildren().elements();		StringBuilder signatureBuilder = new StringBuilder();		StringBuilder desBuilder = new StringBuilder();		boolean preLoading = false;		while (iter.hasMoreNodes()) {			Node _node = iter.nextNode();			String nodeText = _node.getText();			if ("h4".equals(nodeText)) {				field.setName(_node.toPlainTextString());			}			// pre内部的元素会被parser认作为siblings，同时</pre>也会被parser认作为一个元素			if ("pre".equals(nodeText)) { // pre开始				preLoading = true;			}			if ("/pre".equals(nodeText)) { // pre 结束				preLoading = false;			}			if (preLoading) {				String signPart = _node.getNextSibling().toPlainTextString();				if (!BR.equals(signPart)) {					signatureBuilder.append(signPart);				}			}			if ("div class=\"block\"".equals(nodeText)) {				desBuilder.append(_node.toPlainTextString());			}			// dl的toHtml会被parser解析为dl以及其子元素的集合			if ("dl".equals(nodeText)) {				desBuilder.append(_node.toHtml());			}		}		field.setSignature(signatureBuilder.toString());		field.setDescription(desBuilder.toString());		clazz.getFields().add(field);	}	public static Node getFirstNotBRChild(Node node) {		Node child = null;		if (node == null) {			return child;		}		SimpleNodeIterator iter = node.getChildren().elements();		while (iter.hasMoreNodes()) {			child = iter.nextNode();			if (!BR.equals(child.toPlainTextString())) {				break;			}		}		return child;	}	public static void main(String[] args) throws Exception {				//		File[] srcFiles = new File("assets/source").listFiles();//		File target = new File("assets/target");//		for (int i = 0; i < srcFiles.length; i++) {//			File srcFile = srcFiles[i];//			System.out.println("开始解析文件[" + srcFile.getName() + "] (" + (i + 1) + "/" + srcFiles.length + ")");//			Clazz clazz = parseDoc(srcFile.getPath());//			clazz.toXMLFile(target.getPath());//		}//		System.out.println("Conguratulations");	}}