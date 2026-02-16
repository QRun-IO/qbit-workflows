/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qbits.workflows;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataProvider;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.tables.QQQTablesMetaDataProvider;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 **
 *******************************************************************************/
public class BaseTest
{
   public static final String RDBMS_BACKEND_NAME  = "h2rdbms";
   public static final String MEMORY_BACKEND_NAME = "memory";

   public static final String TABLE_NAME_PERSON = "person";
   public static final String TABLE_NAME_SHAPE  = "shape";
   public static final String TABLE_NAME_PET    = "pet";

   public static final String API_NAME             = "test-api";
   public static final String ALTERNATIVE_API_NAME = "person-api";

   public static final String API_PATH             = "/api/";
   public static final String ALTERNATIVE_API_PATH = "/person-api/";

   public static final String V1 = "v1";
   public static final String V2 = "v2";
   public static final String V3 = "v3";

   public static final String CURRENT_API_VERSION = V2;



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void baseBeforeEach() throws Exception
   {
      QInstance qInstance = defineQInstance();
      new QInstanceValidator().validate(qInstance);
      QContext.init(qInstance, new QSession());

      // primeDatabase();

      MemoryRecordStore.fullReset();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void primeDatabase() throws SQLException
   {
      try(Connection connection = ConnectionManager.getConnection((RDBMSBackendMetaData) QContext.getQInstance().getBackend(RDBMS_BACKEND_NAME)))
      {
         InputStream  primeTestDatabaseSqlStream = BaseTest.class.getResourceAsStream("/test-database.sql");
         List<String> lines                      = IOUtils.readLines(primeTestDatabaseSqlStream, StandardCharsets.UTF_8);
         lines = lines.stream().filter(line -> !line.startsWith("-- ")).toList();
         String joinedSQL = String.join("\n", lines);
         for(String sql : joinedSQL.split(";"))
         {
            QueryManager.executeUpdate(connection, sql);
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected QInstance defineQInstance() throws QException
   {
      /////////////////////////////////////
      // basic definition of an instance //
      /////////////////////////////////////
      QInstance qInstance = new QInstance();
      qInstance.setAuthentication(new QAuthenticationMetaData().withType(QAuthenticationType.FULLY_ANONYMOUS));
      qInstance.addBackend(new RDBMSBackendMetaData()
         .withName(RDBMS_BACKEND_NAME)
         .withVendor("h2")
         .withHostName("mem")
         .withDatabaseName("test_database")
         .withUsername("sa"));

      qInstance.addBackend(new QBackendMetaData()
         .withName(MEMORY_BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class));

      new QQQTablesMetaDataProvider().defineAll(qInstance, MEMORY_BACKEND_NAME, MEMORY_BACKEND_NAME, null);

      qInstance.addPossibleValueSource(TablesPossibleValueSourceMetaDataProvider.defineTablesPossibleValueSource(qInstance));

      ////////////////////////
      // configure the qbit //
      ////////////////////////
      WorkflowsQBitConfig config = new WorkflowsQBitConfig()
         .withIncludeApiVersions(true)
         .withTableMetaDataCustomizer((i, table) ->
         {
            if(table.getBackendName() == null)
            {
               table.setBackendName(MEMORY_BACKEND_NAME);
            }

            table.setBackendDetails(new RDBMSTableBackendDetails()
               .withTableName(QInstanceEnricher.inferBackendName(table.getName())));
            QInstanceEnricher.setInferredFieldBackendNames(table);

            return (table);
         });

      //////////////////////
      // produce our qbit //
      //////////////////////
      new WorkflowsQBitProducer()
         .withQBitConfig(config)
         .produce(qInstance)
         .addSelfToInstance(qInstance);

      ///////////////////////////////////////////
      // turn off audits (why on by default??) //
      ///////////////////////////////////////////
      qInstance.getTables().values().forEach(t -> t.setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.NONE)));

      qInstance.addTable(defineTablePerson());
      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(TABLE_NAME_PERSON));
      qInstance.addTable(defineTableShape());
      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(TABLE_NAME_SHAPE));
      qInstance.addJoin(definePersonJoinShape());
      qInstance.addTable(defineTablePet());
      qInstance.addJoin(definePersonJoinPet());
      defineApiMetaData(qInstance);

      return qInstance;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private QJoinMetaData definePersonJoinShape()
   {
      return new QJoinMetaData()
         .withLeftTable(TABLE_NAME_PERSON)
         .withRightTable(TABLE_NAME_SHAPE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("favoriteShapeId", "id"))
         .withName(QJoinMetaData.makeInferredJoinName(TABLE_NAME_PERSON, TABLE_NAME_SHAPE));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private QJoinMetaData definePersonJoinPet()
   {
      return new QJoinMetaData()
         .withLeftTable(TABLE_NAME_PERSON)
         .withRightTable(TABLE_NAME_PET)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "ownerPersonId"))
         .withName(QJoinMetaData.makeInferredJoinName(TABLE_NAME_PERSON, TABLE_NAME_PET));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void defineApiMetaData(QInstance qInstance) throws QException
   {
      qInstance.withSupplementalMetaData(new ApiInstanceMetaDataContainer()
         .withApiInstanceMetaData(new ApiInstanceMetaData()
            .withName(API_NAME)
            .withPath(API_PATH)
            .withLabel("Test API")
            .withDescription("QQQ Test API")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
            .withSupportedVersions(List.of(new APIVersion(V1), new APIVersion(V2)))
            .withPastVersions(List.of(new APIVersion(V1)))
            .withFutureVersions(List.of(new APIVersion(V3))))
         .withApiInstanceMetaData(new ApiInstanceMetaData()
            .withName(ALTERNATIVE_API_NAME)
            .withPath(ALTERNATIVE_API_PATH)
            .withLabel("Person-Only API")
            .withDescription("QQQ Test API, that only has the Person table.")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
            .withSupportedVersions(List.of(new APIVersion(V1), new APIVersion(V2)))
            .withPastVersions(List.of(new APIVersion(V1)))
            .withFutureVersions(List.of(new APIVersion(V3))))
      );

      ApiTableMetaDataContainer.ofOrWithNew(qInstance.getTable(Workflow.TABLE_NAME))
         .withApiTableMetaData(API_NAME, new ApiTableMetaData()
            .withInitialVersion(V1));

      ApiInstanceMetaDataProvider.defineAll(qInstance, MEMORY_BACKEND_NAME, null);
   }



   /*******************************************************************************
    ** Define the 'person' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      QTableMetaData table = new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("firstName", "lastName")
         .withBackendName(MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("email"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("bestFriendPersonId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_PERSON))
         .withField(new QFieldMetaData("favoriteShapeId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_SHAPE))
         .withField(new QFieldMetaData("noOfShoes", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS))
         .withField(new QFieldMetaData("salary", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("debt", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("photo", QFieldType.BLOB));

      table.withExposedJoin(new ExposedJoin()
         .withJoinTable(TABLE_NAME_SHAPE)
         .withJoinPath(List.of(QJoinMetaData.makeInferredJoinName(TABLE_NAME_PERSON, TABLE_NAME_SHAPE)))
         .withLabel("Favorite Shape"));

      table.withExposedJoin(new ExposedJoin()
         .withJoinTable(TABLE_NAME_PET)
         .withJoinPath(List.of(QJoinMetaData.makeInferredJoinName(TABLE_NAME_PERSON, TABLE_NAME_PET)))
         .withLabel("Pet"));

      table.withAssociation(new Association()
         .withAssociatedTableName(TABLE_NAME_PET)
         .withJoinName(QJoinMetaData.makeInferredJoinName(TABLE_NAME_PERSON, TABLE_NAME_PET))
         .withName("pets"));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // make some changes to this table in the "main" api (but leave it like the backend in the ALTERNATIVE_API_NAME) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      table.withSupplementalMetaData(new ApiTableMetaDataContainer()
         .withApiTableMetaData(API_NAME, new ApiTableMetaData()
            .withInitialVersion(V1)

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // in 2022.Q4, this table had a "shoeCount" field. but for the 2023.Q1 version, we renamed it to noOfShoes! //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            .withRemovedApiField(new QFieldMetaData("shoeCount", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS)
               .withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME,
                  new ApiFieldMetaData().withFinalVersion(V1).withReplacedByFieldName("noOfShoes"))))
         )
         .withApiTableMetaData(ALTERNATIVE_API_NAME, new ApiTableMetaData().withInitialVersion(V1)));

      /////////////////////////////////////////////////////
      // change the name for this field for the main api //
      /////////////////////////////////////////////////////
      table.getField("birthDate").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withApiFieldName("birthDay")));

      ///////////////////////////////////////////////////////////////
      // See above - we renamed this field (in the backend) for V2 //
      ///////////////////////////////////////////////////////////////
      table.getField("noOfShoes").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withInitialVersion(V2)));

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // 2 new fields - one will appear in a future version of the API, the other is always excluded //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      table.getField("salary").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withInitialVersion(V3)));
      table.getField("debt").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withIsExcluded(true)));

      return (table);
   }



   /*******************************************************************************
    ** Define the 'shape' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableShape()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_SHAPE)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withRecordLabelFields("name")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("type", QFieldType.STRING)) // todo PVS
         .withField(new QFieldMetaData("noOfSides", QFieldType.INTEGER))
         .withField(new QFieldMetaData("isPolygon", QFieldType.BOOLEAN)) // mmm, should be derived from type, no?
         ;
   }



   /*******************************************************************************
    ** Define the 'pet' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePet()
   {
      QTableMetaData table = new QTableMetaData()
         .withName(TABLE_NAME_PET)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withRecordLabelFields("name")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("species", QFieldType.STRING)) // todo PVS
         .withField(new QFieldMetaData("ownerPersonId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_PERSON));;

      table.withSupplementalMetaData(new ApiTableMetaDataContainer()
         .withApiTableMetaData(API_NAME, new ApiTableMetaData()
            .withInitialVersion(V1)));

      return table;
   }

}
