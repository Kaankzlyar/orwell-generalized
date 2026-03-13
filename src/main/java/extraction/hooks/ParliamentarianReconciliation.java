package extraction.hooks;

import java.util.List;

import extraction.Hook;
import extraction.ProcessingContext;
import extraction.XMLObject;

public class ParliamentarianReconciliation extends Hook {
    @Override
    public void execute(ProcessingContext context) {
        List<XMLObject> legislatureDocs = loadDocuments("informacaobase");

        for (XMLObject doc : legislatureDocs) {
            String legislature = getLegislature(doc);
            List<XMLObject> parliamentarians = doc.findElementsByName("DadosDeputadoOrgaoPlenario");
            for (XMLObject parliamentarian : parliamentarians) {
                String id = parliamentarian.findFirstElementByName("DepCadId").getText();
                String name = parliamentarian.findFirstElementByName("DepNomeParlamentar").getText();
                String key = legislature + ":" + name;

                log("Parliamentarian with key " + key + " and id " + id);
            }
        }
    }

    @Override
    public String getName() {
        return "ParliamentarianReconciliation";
    }

    private String getLegislature(XMLObject doc){
        XMLObject sigla = doc.findFirstElementByName("sigla");
        if (sigla == null) {
            throw new IllegalStateException("Missing sigla in informacaobase document.");
        }
        String text = sigla.getText();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Empty sigla in informacaobase document.");
        }
        return text;
    }
}
