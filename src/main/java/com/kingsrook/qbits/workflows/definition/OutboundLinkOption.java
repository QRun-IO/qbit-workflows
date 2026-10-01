/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kingsrook.qbits.workflows.definition;


import java.util.List;


/*******************************************************************************
 ** for a stepType with more than one outbound links, e.g., with conditional guards
 ** on them, an instance of this class describes one such link (e.g., "true" labeled
 ** for users as "Then").
 *******************************************************************************/
public class OutboundLinkOption
{
   private String value;
   private String label;

   private List<String> stepTypesToIncludeByDefault;



   /*******************************************************************************
    ** Getter for value
    *******************************************************************************/
   public String getValue()
   {
      return (this.value);
   }



   /*******************************************************************************
    ** Setter for value
    *******************************************************************************/
   public void setValue(String value)
   {
      this.value = value;
   }



   /*******************************************************************************
    ** Fluent setter for value
    *******************************************************************************/
   public OutboundLinkOption withValue(String value)
   {
      this.value = value;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public OutboundLinkOption withLabel(String label)
   {
      this.label = label;
      return (this);
   }


   /*******************************************************************************
    * Getter for stepTypesToIncludeByDefault
    * @see #withStepTypesToIncludeByDefault(List)
    *******************************************************************************/
   public List<String> getStepTypesToIncludeByDefault()
   {
      return (this.stepTypesToIncludeByDefault);
   }



   /*******************************************************************************
    * Setter for stepTypesToIncludeByDefault
    * @see #withStepTypesToIncludeByDefault(List)
    *******************************************************************************/
   public void setStepTypesToIncludeByDefault(List<String> stepTypesToIncludeByDefault)
   {
      this.stepTypesToIncludeByDefault = stepTypesToIncludeByDefault;
   }



   /*******************************************************************************
    * Fluent setter for stepTypesToIncludeByDefault
    *
    * @param stepTypesToIncludeByDefault for the use-case where, you want to have a
    * step type, that when you add it to a workflow, it implicitly contains some
    * sub-steps within one of its outbound links.  e.g., an on-failure branch that
    * calls some exception handling step.  Put the names of the step types to include
    * in this list.  Leave null to omit this behavior.
    * @return this
    *******************************************************************************/
   public OutboundLinkOption withStepTypesToIncludeByDefault(List<String> stepTypesToIncludeByDefault)
   {
      this.stepTypesToIncludeByDefault = stepTypesToIncludeByDefault;
      return (this);
   }


}
