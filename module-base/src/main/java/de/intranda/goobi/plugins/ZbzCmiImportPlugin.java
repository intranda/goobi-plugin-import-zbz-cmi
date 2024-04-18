package de.intranda.goobi.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPluginVersion2;
import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.goobi.production.properties.ImportProperty;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.forms.MassImportForm;
import de.sub.goobi.helper.exceptions.ImportPluginException;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.WriteException;

@PluginImplementation
@Log4j2
public class ZbzCmiImportPlugin implements IImportPluginVersion2 {

    @Getter
    private String title = "intranda_import_zbz_cmi";
    @Getter
    private PluginType type = PluginType.Import;

    @Getter
    private List<ImportType> importTypes;

    @Getter
    @Setter
    private Prefs prefs;
    @Getter
    @Setter
    private String importFolder;

    @Setter
    private MassImportForm form;

    @Setter
    private boolean testMode = false;

    @Getter
    @Setter
    private File file;

    @Setter
    private String workflowTitle;
    private boolean runAsGoobiScript = false;
    private String identifier;
    private Fileformat fileformat = null;
    private IOpacPlugin opacPlugin = null;
    private String catalogue = "";
    private String searchField = "";

    /**
     * define what kind of import plugin this is
     */
    public ZbzCmiImportPlugin() {
        importTypes = new ArrayList<>();
        importTypes.add(ImportType.Record);
    }

    /**
     * read the configuration file
     */
    private void readConfig() {
        XMLConfiguration xmlConfig = ConfigPlugins.getPluginConfig(title);
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        SubnodeConfiguration myconfig = null;
        try {
            myconfig = xmlConfig.configurationAt("//config[./template = '" + workflowTitle + "']");
        } catch (IllegalArgumentException e) {
            myconfig = xmlConfig.configurationAt("//config[./template = '*']");
        }

        if (myconfig != null) {
            runAsGoobiScript = myconfig.getBoolean("/runAsGoobiScript", false);
            catalogue = myconfig.getString("/catalogue", "");
            searchField = myconfig.getString("/searchField", "");
        }
    }

    @Override
    public List<ImportObject> generateFiles(List<Record> records) {
        List<ImportObject> importedRecords = new LinkedList<>();
        for (Record record : records) {
            if (form != null) {
                form.addProcessToProgressBar();
            }
            ImportObject io = new ImportObject();
            importedRecords.add(io);
            log.info(record.getId());
            identifier = record.getId();
            if (StringUtils.isNotBlank(identifier)) {
                try {
                    fileformat = convertData();
                    if (fileformat != null) {
                        DocStruct anchor = null;
                        DocStruct logical = fileformat.getDigitalDocument().getLogicalDocStruct();
                        if (logical.getType().isAnchor()) {
                            anchor = logical;
                            logical = logical.getAllChildren().get(0);
                        }

                        // and add all collections that where selected
                        for (String colItem : form.getDigitalCollections()) {
                            Metadata mdColl = new Metadata(prefs.getMetadataTypeByName("singleDigCollection"));
                            mdColl.setValue(colItem);
                            logical.addMetadata(mdColl);

                            if (anchor != null) {
                                mdColl = new Metadata(prefs.getMetadataTypeByName("singleDigCollection"));
                                mdColl.setValue(colItem);
                                anchor.addMetadata(mdColl);
                            }
                        }

                        io.setProcessTitle(identifier);
                        fileformat.write(importFolder + record.getId() + ".xml");
                        io.setMetsFilename(importFolder + record.getId() + ".xml");
                        io.setImportReturnValue(ImportReturnValue.ExportFinished);

                    } else {
                        log.info("Can't find record for id " + identifier);
                        io.setErrorMessage(record.getId() + ": error during opac request.");
                        io.setImportReturnValue(ImportReturnValue.InvalidData);
                        io.setProcessTitle(identifier);
                    }

                } catch (ImportPluginException | PreferencesException | MetadataTypeNotAllowedException | WriteException e) {
                    log.error(e);
                }
            }
        }

        return importedRecords;
    }

    /**
     * decide if the import shall be executed in the background via GoobiScript or not
     */
    @Override
    public boolean isRunnableAsGoobiScript() {
        readConfig();
        return runAsGoobiScript;
    }

    @Override
    public List<Record> splitRecords(String content) {
        if (StringUtils.isBlank(workflowTitle)) {
            workflowTitle = form.getTemplate().getTitel();
        }
        readConfig();

        // the list where the records are stored
        List<Record> recordList = new ArrayList<>();

        // run through the content line by line
        String lines[] = content.split("\\r?\\n");

        // generate a record for each process to be created
        for (String line : lines) {
            Record r = new Record();
            r.setId(line);
            recordList.add(r);
        }

        // return the list of all generated records
        return recordList;
    }

    /* *************************************************************** */
    /*                                                                 */
    /* the following methods are mostly not needed for typical imports */
    /*                                                                 */
    /* *************************************************************** */

    /**
     * This method is used to generate records based on the imported data these records will then be used later to generate the Goobi processes
     */
    @Override
    public List<Record> generateRecordsFromFile() {
        return null;
    }

    @Override
    public List<String> splitIds(String ids) {
        return null;
    }

    @Override
    public String addDocstruct() {
        return null;
    }

    @Override
    public String deleteDocstruct() {
        return null;
    }

    @Override
    public void deleteFiles(List<String> arg0) {
    }

    @Override
    public List<Record> generateRecordsFromFilenames(List<String> arg0) {
        return null;
    }

    @Override
    public List<String> getAllFilenames() {
        return null;
    }

    @Override
    public List<? extends DocstructElement> getCurrentDocStructs() {
        return null;
    }

    @Override
    public DocstructElement getDocstruct() {
        return null;
    }

    @Override
    public List<String> getPossibleDocstructs() {
        return null;
    }

    @Override
    public String getProcessTitle() {
        return null;
    }

    @Override
    public List<ImportProperty> getProperties() {
        return null;
    }

    @Override
    public void setData(Record arg0) {
    }

    @Override
    public void setDocstruct(DocstructElement arg0) {
    }

    @Override
    public Fileformat convertData() throws ImportPluginException {
        readConfig();
        fileformat = null;
        ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(catalogue);
        if (opacPlugin == null) {
            opacPlugin = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, coc.getOpacType());
        }
        try {
            fileformat = opacPlugin.search(searchField, identifier, coc, prefs);
        } catch (Exception e) {
            throw new ImportPluginException(e);
        }
        return fileformat;
    }

}