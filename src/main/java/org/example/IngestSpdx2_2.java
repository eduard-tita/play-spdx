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

import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.jacksonstore.MultiFormatStore.Format;
import org.spdx.jacksonstore.MultiFormatStore.Verbose;
import org.spdx.library.DefaultModelStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.ExternalRef;
import org.spdx.library.model.ReferenceType;
import org.spdx.library.model.Relationship;
import org.spdx.library.model.SpdxCreatorInformation;
import org.spdx.library.model.SpdxDocument;
import org.spdx.library.model.SpdxElement;
import org.spdx.library.model.SpdxPackage;
import org.spdx.library.model.enumerations.ReferenceCategory;
import org.spdx.library.model.enumerations.RelationshipType;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;

public class IngestSpdx2_2
{
  public static void main(String[] args) throws InvalidSPDXAnalysisException, IOException {

    String filename = "appbomination.spdx.json";

    System.out.print("Deserializing from " + filename + " ... ");
    IModelStore modelStore = new InMemSpdxStore();
    MultiFormatStore multiFormatStore = new MultiFormatStore(modelStore, Format.JSON, Verbose.COMPACT);
    try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
      String documentNamespace = multiFormatStore.deSerialize(in, true);
      System.out.println("documentNamespace: " + documentNamespace);

      SpdxDocument spdxDocument = new SpdxDocument(modelStore, documentNamespace, DefaultModelStore.getDefaultCopyManager(), true);
      System.out.println("spdxDocument: " + spdxDocument);

      final String specVersion = spdxDocument.getSpecVersion();
      System.out.println("specVersion: " + specVersion);
    }
  }
}
