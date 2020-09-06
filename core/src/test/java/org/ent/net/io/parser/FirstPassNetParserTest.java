package org.ent.net.io.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class FirstPassNetParserTest {

	@Test
	void resolveIdentifier_chain() throws Exception {
		String input = "A=B; B=C; C=<nop>";
		FirstPassNetParser parser = new FirstPassNetParser(new StringReader(input));
		List<NodeTemplate> mainNodeTemplates = parser.parseAll();

		assertThat(mainNodeTemplates).hasSize(3);
		Set<NodeTemplate> allEntries = parser.getAllEntries();
		assertThat(allEntries).hasSize(3);
		Map<String, NodeTemplate> identifierMapping = parser.getIdentifierMapping();
		assertThat(identifierMapping).hasSize(3);
		assertThat(identifierMapping).containsOnlyKeys("A", "B", "C");

		NodeTemplate template1 = identifierMapping.get("A");
		NodeTemplate template2 = identifierMapping.get("B");
		NodeTemplate template3 = identifierMapping.get("C");

		assertThat(identifierMapping).containsOnly(entry("A", template1), entry("B", template2), entry("C", template3));

		assertThat(template1).isInstanceOfSatisfying(IdentifierNodeTemplate.class, i -> {
			assertThat(i.getName()).isEqualTo("B");
		});
		assertThat(template2).isInstanceOfSatisfying(IdentifierNodeTemplate.class, i -> {
			assertThat(i.getName()).isEqualTo("C");
		});
		assertThat(template3).isInstanceOfSatisfying(CommandNodeTemplate.class, c -> {
			assertThat(c.getCommandName()).isEqualTo("nop");
		});

		assertThat(parser.resolveIdentifier((IdentifierNodeTemplate) template1)).isSameAs(template3);
		assertThat(parser.resolveIdentifier((IdentifierNodeTemplate) template2)).isSameAs(template3);
	}

	@Test
	void parseAll_okay_semicolons() throws Exception {
		String input = "; <nop> ;;; <nop>;";
		FirstPassNetParser parser = new FirstPassNetParser(new StringReader(input));
		parser.parseAll();
		assertThat(parser.getAllEntries().size()).isEqualTo(2);
	}

	@Test
	void resolveIdentifier_error_cycle() throws Exception {
		String input = "A=B; B=C; C=A";
		FirstPassNetParser parser = new FirstPassNetParser(new StringReader(input));
		parser.parseAll();
		Map<String, NodeTemplate> identifierMapping = parser.getIdentifierMapping();
		assertThat(identifierMapping.size()).isEqualTo(3);
		NodeTemplate template1 = identifierMapping.get("A");
		assertThatThrownBy(() -> parser.resolveIdentifier((IdentifierNodeTemplate) template1))
				.isInstanceOf(ParserException.class).hasMessage("Cyclic identifier binding");
	}

	@Test
	void parseAll_error_duplicateName() throws Exception {
		String input = "(A=<nop>, A=[<nop>])";
		FirstPassNetParser parser = new FirstPassNetParser(new StringReader(input));
		assertThatThrownBy(() -> parser.parseAll())
				.isInstanceOf(ParserException.class).hasMessage("Identifier 'A' bound more than once.");
	}
}
