package org.ent.net.io.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class FirstPassNetParserTest {

	@Test
	public void resolveIdentifier_chain() throws Exception {
		String input = "A=B; B=C; C=<nop>";
		FirstPassNetParser parser = new FirstPassNetParser(new StringReader(input));
		List<NodeTemplate> mainNodeTemplates = parser.parseAll();

		assertThat(mainNodeTemplates.size()).isEqualTo(3);
		Set<NodeTemplate> allEntries = parser.getAllEntries();
		assertThat(allEntries.size()).isEqualTo(3);
		Map<String, NodeTemplate> identifierMapping = parser.getIdentifierMapping();
		assertThat(identifierMapping.size()).isEqualTo(3);
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
	public void resolveIdentifier_error_cycle() throws Exception {
		String input = "A=B; B=C; C=A";
		FirstPassNetParser parser = new FirstPassNetParser(new StringReader(input));
		parser.parseAll();
		Map<String, NodeTemplate> identifierMapping = parser.getIdentifierMapping();
		assertThat(identifierMapping.size()).isEqualTo(3);
		NodeTemplate template1 = identifierMapping.get("A");
		Assertions.assertThatThrownBy(() -> parser.resolveIdentifier((IdentifierNodeTemplate) template1))
				.isInstanceOf(ParserException.class).hasMessage("Cyclic identifier binding");
	}

}
