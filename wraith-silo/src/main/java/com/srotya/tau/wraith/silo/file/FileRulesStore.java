/**
 * Copyright 2016 Symantec Corporation.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.srotya.tau.wraith.silo.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.srotya.tau.wraith.actions.alerts.templated.AlertTemplate;
import com.srotya.tau.wraith.actions.alerts.templated.AlertTemplateSerializer;
import com.srotya.tau.wraith.actions.alerts.templated.TemplateCommand;
import com.srotya.tau.wraith.rules.Rule;
import com.srotya.tau.wraith.rules.RuleCommand;
import com.srotya.tau.wraith.rules.RuleSerializer;
import com.srotya.tau.wraith.rules.SimpleRule;
import com.srotya.tau.wraith.store.RulesStore;
import com.srotya.tau.wraith.store.TemplateStore;

/**
 * @author ambud_sharma
 *
 */
public class FileRulesStore implements RulesStore, TemplateStore {

	private static final Logger logger = Logger.getLogger(FileRulesStore.class.getName());
	public static final String CONF_RULES_DIR = "store.rules.dir";
	public static final String CONF_TEMPLATES_DIR = "store.templates.dir";
	private String rulesDirectory;
	private String templatesDirectory;
	private File[] ruleFiles;
	private File[] templateFiles;

	@Override
	public void initialize(Map<String, String> conf) {
		rulesDirectory = conf.get(CONF_RULES_DIR);
		templatesDirectory = conf.get(CONF_TEMPLATES_DIR);
		logger.info("Rule directory:"+rulesDirectory+"\tTemplate directory:"+templatesDirectory+"\t"+conf);
	}

	@Override
	public void connect() throws IOException {
		File rd = new File(rulesDirectory);
		if (!rd.exists()) {
			throw new IOException("Rules directory doesn't exist");
		}
		ruleFiles = rd.listFiles();

		rd = new File(templatesDirectory);
		if (!rd.exists()) {
			throw new IOException("Templates directory doesn't exist");
		}
		templateFiles = rd.listFiles();
	}

	@Override
	public void disconnect() throws IOException {
	}

	protected void addRulesFromLines(Map<Short, Rule> rules, List<String> lines) {
		for (String line : lines) {
			if (line == null || line.trim().isEmpty()) {
				continue;
			}
			SimpleRule rule = RuleSerializer.deserializeJSONStringToRule(line);
			rules.put(rule.getRuleId(), rule);
		}
	}

	@Override
	public Map<String, Map<Short, Rule>> listGroupedRules() throws IOException {
		Map<String, Map<Short, Rule>> rules = new HashMap<>();
		for (File file : ruleFiles) {
			List<String> lines = Files.readAllLines(file.toPath());
			addRulesGroupFromLines(rules, lines);
		}
		return rules;
	}

	protected void addRulesGroupFromLines(Map<String, Map<Short, Rule>> rules, List<String> lines) {
		Gson gson = new Gson();
		for (String line : lines) {
			if (line == null || line.trim().isEmpty()) {
				continue;
			}
			RuleCommand cmd = gson.fromJson(line, RuleCommand.class);
			SimpleRule rule = RuleSerializer.deserializeJSONStringToRule(cmd.getRuleContent());
			Map<Short, Rule> grp = rules.get(cmd.getRuleGroup());
			if (grp == null) {
				rules.put(cmd.getRuleGroup(), grp);
			}
			grp.put(rule.getRuleId(), rule);
		}
	}
	
	protected void addTemplatesGroupFromLines(Map<Short, AlertTemplate> templates, List<String> lines) {
		Gson gson = new Gson();
		for (String line : lines) {
			if (line == null || line.trim().isEmpty()) {
				continue;
			}
			TemplateCommand cmd = gson.fromJson(line, TemplateCommand.class);
			AlertTemplate template = AlertTemplateSerializer.deserialize(cmd.getTemplateContent());
			templates.put(template.getTemplateId(), template);
		}
	}

	@Override
	public Map<Short, AlertTemplate> getAllTemplates() throws IOException {
		Map<Short, AlertTemplate> templates = new HashMap<>();
		for (File file : templateFiles) {
			List<String> lines = Files.readAllLines(file.toPath());
			addTemplatesGroupFromLines(templates, lines);
		}
		return templates;
	}

}