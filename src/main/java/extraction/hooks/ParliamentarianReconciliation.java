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
            String legislature = doc.getAttribute("sigla").get();
            List<XMLObject> parliamentarians = doc.findElementsByName("DadosDeputadoOrgaoPlenario");
            for (XMLObject parliamentarian : parliamentarians) {
                String id = parliamentarian.getAttribute("DepCadId").get();
                String name = parliamentarian.getAttribute("DepNomeParlamentar").get().trim().toLowerCase();
                String key = legislature + ":" + name;
                
                registerLookupTable(context, key, id);
            }
        }
    }

    @Override
    public String getName() {
        return "ParliamentarianReconciliation";
    }
}
