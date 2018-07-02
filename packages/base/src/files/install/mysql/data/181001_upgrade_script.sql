-- ============LICENSE_START=======================================================
-- ONAP Policy Engine
-- ================================================================================
-- Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
-- ================================================================================
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- ============LICENSE_END=========================================================
*/
use onap_sdk;

drop table if exists `onap_sdk`.`namingsequences`
CREATE TABLE `onap_sdk`.`namingsequences` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `namingtype` varchar(64) NOT NULL,
  `scope` varchar(64) NOT NULL,
  `sequencekey` varchar(256) NOT NULL,
  `generatedName` varchar(256),
  `startrange` bigint(20) NOT NULL,
  `endrange` bigint(20) NOT NULL,
  `steprange` bigint(20) NOT NULL,
  `currentseq` bigint(20) NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `namingsequences_type_scope_seq` (`namingtype`,`scope`,`sequencekey`),
  KEY `namingsequences_type_gen_name` (`namingtype`,`scope`,`generatedName`),
  constraint `uniq_naming_seq_001` UNIQUE (`namingtype`,`scope`,`sequencekey`, `currentseq`)
 );

ALTER TABLE `onap_sdk`.`microservicemodels` 
ADD COLUMN `decisionModel` tinyint(1) DEFAULT NULL;