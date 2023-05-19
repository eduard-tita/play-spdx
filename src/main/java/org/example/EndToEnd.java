package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.jacksonstore.MultiFormatStore.Format;
import org.spdx.jacksonstore.MultiFormatStore.Verbose;
import org.spdx.library.DefaultModelStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.ExternalRef;
import org.spdx.library.model.ModelObject;
import org.spdx.library.model.ReferenceType;
import org.spdx.library.model.Relationship;
import org.spdx.library.model.SpdxCreatorInformation;
import org.spdx.library.model.SpdxDocument;
import org.spdx.library.model.SpdxElement;
import org.spdx.library.model.SpdxModelFactory;
import org.spdx.library.model.SpdxPackage;
import org.spdx.library.model.TypedValue;
import org.spdx.library.model.enumerations.ReferenceCategory;
import org.spdx.library.model.enumerations.RelationshipType;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;

public class EndToEnd
{
  private static Map<String, SpdxElement> elementMap = new HashMap<>();
  
  private static final String URI = "https://github.com/sonatype/maven-test-app";

  public static void main(String[] args) throws InvalidSPDXAnalysisException, IOException {

    SpdxDocument spdxDocument = createDocument();

    // JSON
    serialize(spdxDocument, "first.spdx.json", Format.JSON_PRETTY, URI);
    SpdxDocument spdxDocument2 = deserialize("first.spdx.json", Format.JSON, URI);
    serialize(spdxDocument2, "second.spdx.json", Format.JSON_PRETTY, URI);
    System.out.println(" ");

    // XML
    serialize(spdxDocument, "first.spdx.xml", Format.XML, URI);
    spdxDocument2 = deserialize("first.spdx.xml", Format.XML, URI);
    serialize(spdxDocument2, "second.spdx.xml", Format.XML, URI);
    System.out.println(" ");

    // YAML
    //serialize(spdxDocument, "first.spdx.yaml", Format.YAML, URI);
    //spdxDocument2 = deserialize("first.spdx.yaml", Format.YAML, URI);
    //serialize(spdxDocument2, "second.spdx.yaml", Format.YAML, URI);
  }

  static void serialize(SpdxDocument spdxDocument, String filename, Format format, String uri)
      throws IOException, InvalidSPDXAnalysisException
  {
    System.out.print("Serializing to " + filename + " ... ");
    MultiFormatStore multiFormatStore = new MultiFormatStore(spdxDocument.getModelStore(), format, Verbose.STANDARD);
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
      multiFormatStore.serialize(uri, out);
    }
    System.out.println("OK");
  }

  static SpdxDocument deserialize(String filename, Format format, String uri)
      throws IOException, InvalidSPDXAnalysisException
  {
    System.out.print("Deserializing from " + filename + " ... ");
    IModelStore modelStore = new InMemSpdxStore();
    MultiFormatStore multiFormatStore = new MultiFormatStore(modelStore, format, Verbose.COMPACT);
    try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
      String documentNamespace = multiFormatStore.deSerialize(in, true);
    }
    SpdxDocument spdxDocument = new SpdxDocument(modelStore, uri, DefaultModelStore.getDefaultCopyManager(), true);
    System.out.println("OK");
    return spdxDocument;
  }

  static SpdxDocument createDocument() throws InvalidSPDXAnalysisException {
    SpdxDocument spdxDocument = new SpdxDocument(URI);
    spdxDocument.setSpecVersion("SPDX-2.3");

    SpdxCreatorInformation creationInfo = new SpdxCreatorInformation();
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .appendLiteral('T')
        .appendPattern("HH:mm:ss")
        .appendLiteral('Z');
    String date = LocalDateTime.now().format(builder.toFormatter());

    creationInfo.setCreated(date);
    creationInfo.setComment("Creation comment");
    creationInfo.getCreators().add("Tool: Sonatype IQ Server - 1.160.0-01");
    spdxDocument.setCreationInfo(creationInfo);

    addRootPackage(spdxDocument);
    addLog4jCorePackage(spdxDocument);
    addLog4jApiPackage(spdxDocument);

    return spdxDocument;
  }

  static void addRootPackage(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {
    spdxDocument.setName("pkg:maven/org.test.maven/maven-test-app@1.0.0-SNAPSHOT?type=jar");

    ExternalRef externalRef =
        spdxDocument.createExternalRef(ReferenceCategory.PACKAGE_MANAGER, new ReferenceType("purl"),
            "pkg:maven/org.test.maven/maven-test-app@1.0.0-SNAPSHOT?type=jar", null);

    ExternalRef externalRef1 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "file://maven-test-app.bom.json", "type: CycloneDX");

    SpdxPackage pkg =
        spdxDocument.createPackage("SPDXRef-maven-org.test.maven-maven-test-app-1.0.0-SNAPSHOT",
                "org.test.maven:maven-test-app",
                new SpdxListedLicense("Apache-2.0"), null, new SpdxListedLicense("Apache-2.0"))
            .setVersionInfo("1.0.0-SNAPSHOT")
            .setFilesAnalyzed(false)
            .addExternalRef(externalRef)
            .addExternalRef(externalRef1)
            .build();
    elementMap.put(pkg.getId(), pkg);

    spdxDocument.getDocumentDescribes().add(pkg);
  }

  static void addLog4jCorePackage(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {

    ExternalRef externalRef =
        spdxDocument.createExternalRef(ReferenceCategory.PACKAGE_MANAGER, new ReferenceType("purl"),
            "pkg:maven/org.apache.logging.log4j/log4j-core@2.8.1?type=jar", null);

    ExternalRef externalRef1 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://localhost:8070/ui/links/vln/sonatype-2021-4560", "source: SONATYPE");
    ExternalRef externalRef2 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2020-9488", "source: NVD");
    ExternalRef externalRef3 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-45046", "source: NVD");
    ExternalRef externalRef4 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-5645", "source: NVD");
    ExternalRef externalRef5 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-45105", "source: NVD");
    ExternalRef externalRef6 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228", "source: NVD");
    ExternalRef externalRef7 =
        spdxDocument.createExternalRef(ReferenceCategory.SECURITY, new ReferenceType("advisory"),
            "http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44832", "source: NVD");

    SpdxPackage pkg =
        spdxDocument.createPackage("SPDXRef-maven-org.apache.logging.log4j-log4j-core-2.8.1",
                "org.apache.logging.log4j:log4j-core",
                new SpdxListedLicense("Apache-2.0"), null, new SpdxListedLicense("Apache-2.0"))
            .setVersionInfo("2.8.1")
            .setFilesAnalyzed(false)
            .addExternalRef(externalRef)
            .addExternalRef(externalRef1)
            .addExternalRef(externalRef2)
            .addExternalRef(externalRef3)
            .addExternalRef(externalRef4)
            .addExternalRef(externalRef5)
            .addExternalRef(externalRef6)
            .addExternalRef(externalRef7)
            .build();
    elementMap.put(pkg.getId(), pkg);

    Relationship relationship =
        spdxDocument.createRelationship(elementMap.get("SPDXRef-maven-org.test.maven-maven-test-app-1.0.0-SNAPSHOT"),
            RelationshipType.DEPENDENCY_OF, null);
    pkg.addRelationship(relationship);
  }

  static void addLog4jApiPackage(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {

    ExternalRef externalRef =
        spdxDocument.createExternalRef(ReferenceCategory.PACKAGE_MANAGER, new ReferenceType("purl"),
            "pkg:maven/org.apache.logging.log4j/log4j-api@2.8.1?type=jar", null);

    SpdxPackage pkg =
        spdxDocument.createPackage("SPDXRef-maven-org.apache.logging.log4j-log4j-api-2.8.1",
                "org.apache.logging.log4j:log4j-api",
                new SpdxListedLicense("Apache-2.0"), null, new SpdxListedLicense("Apache-2.0"))
            .setVersionInfo("2.8.1")
            .setFilesAnalyzed(false)
            .addExternalRef(externalRef)
            .build();
    elementMap.put(pkg.getId(), pkg);

    Relationship relationship =
        spdxDocument.createRelationship(elementMap.get("SPDXRef-maven-org.apache.logging.log4j-log4j-core-2.8.1"),
            RelationshipType.DEPENDENCY_OF, null);
    pkg.addRelationship(relationship);
  }
}
