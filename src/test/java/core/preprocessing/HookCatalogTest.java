package core.preprocessing;

  import static org.junit.jupiter.api.Assertions.assertEquals;
  import static org.junit.jupiter.api.Assertions.assertThrows;
  import static org.junit.jupiter.api.Assertions.assertTrue;

  import java.nio.file.Path;
  import java.util.List;

  import org.junit.jupiter.api.Test;

  class HookCatalogTest {

      private static final Path MANIFEST = Path.of("usecases", "test", "dataset.yml");

      @Test
      void resolvesHooksInManifestOrder() {
          Hook first = new NamedHook("First");
          Hook second = new NamedHook("Second");
          HookCatalog catalog = new HookCatalog(List.of(first, second));

          assertEquals(List.of(second, first), catalog.resolve(List.of("Second", "First"), MANIFEST));
      }

      @Test
      void resolvesEmptyListToNoHooks() {
          HookCatalog catalog = new HookCatalog(List.of(new NamedHook("First")));

          assertEquals(List.of(), catalog.resolve(List.of(), MANIFEST));
      }

      @Test
      void rejectsUnknownNameWithManifestAndAvailableNames() {
          HookCatalog catalog = new HookCatalog(List.of(new NamedHook("First")));

          IllegalStateException e = assertThrows(IllegalStateException.class,
              () -> catalog.resolve(List.of("First", "Missing"), MANIFEST));

          assertTrue(e.getMessage().contains("'Missing'"));
          assertTrue(e.getMessage().contains(MANIFEST.toString()));
          assertTrue(e.getMessage().contains("[First]"));
      }

      @Test
      void matchesNamesCaseSensitively() {
          HookCatalog catalog = new HookCatalog(List.of(new NamedHook("First")));

          assertThrows(IllegalStateException.class, () -> catalog.resolve(List.of("first"), MANIFEST));
      }

      @Test
      void rejectsDuplicateNamesNamingBothClasses() {
          IllegalStateException e = assertThrows(IllegalStateException.class,
              () -> new HookCatalog(List.of(new NamedHook("Same"), new OtherHook("Same"))));

          assertTrue(e.getMessage().contains("'Same'"));
          assertTrue(e.getMessage().contains(NamedHook.class.getName()));
          assertTrue(e.getMessage().contains(OtherHook.class.getName()));
      }

      @Test
      void serviceLoaderDiscoversParliamentHooks() {
          List<String> names = List.of(
              "RemoveEmptyXmlElements",
              "ParliamentarianIdentification",
              "CommissionInformation",
              "LegislatureInformation",
              "ExtractVoting",
              "AddLegislatureToVotes"
          );

          List<Hook> hooks = HookCatalog.fromServiceLoader().resolve(names, MANIFEST);

          assertEquals(names, hooks.stream().map(Hook::getName).toList());
      }

      private static class NamedHook extends Hook {
          private final String name;

          NamedHook(String name) {
              this.name = name;
          }

          @Override
          public void execute(ProcessingContext context) {
          }

          @Override
          public String getName() {
              return name;
          }
      }

      private static class OtherHook extends NamedHook {
          OtherHook(String name) {
              super(name);
          }
      }
  }