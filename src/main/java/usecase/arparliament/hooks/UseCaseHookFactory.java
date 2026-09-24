package usecase.arparliament.hooks;

import java.util.List;

import core.preprocessing.Hook;

public final class UseCaseHookFactory {

    private UseCaseHookFactory() {}

    public static List<Hook> resolve(List<String> hookNames) {
        return hookNames.stream().map(UseCaseHookFactory::resolveHook).toList();
    }

    private static Hook resolveHook(String name) {
        return switch (name) {
            case "RemoveEmptyXmlElements" -> new RemoveEmptyXmlElements();
            case "ParliamentarianIdentification" -> new ParliamentarianIdentification();
            case "CommissionInformation" -> new CommissionInformation();
            case "LegislatureInformation" -> new LegislatureInformation();
            case "ExtractVoting" -> new ExtractVoting();
            case "AddLegislatureToVotes" -> new AddLegislatureToVotes();
            default -> throw new IllegalStateException("Unknown hook: " + name);
        };
    }
}
