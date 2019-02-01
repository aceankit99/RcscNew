/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.california.service;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.primefaces.california.domain.Document;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "documentService")
@ApplicationScoped
public class DocumentService {

    public TreeNode createDocuments() {
        TreeNode root = new DefaultTreeNode(new Document("Files", "-", "Folder"), null);

        TreeNode ru1015 = new DefaultTreeNode(new Document("RU1015 FPD ENG'D MOOSIC PARTS DISTR", "-", "Folder", "AMSS", "-", "-", "-"), root);
        ru1015.setExpanded(true);
        TreeNode ru1100 = new DefaultTreeNode(new Document("RU1100 FPD ENG'D TANEYTOWN MD", "-", "Folder", "IPO", "-", "-", "-"), root);
        TreeNode ru1200 = new DefaultTreeNode(new Document("RU1200 FPD ENG'D CPO VERNON", "-", "Folder", "EPO", "-", "-", "-"), root);

        TreeNode chevron = new DefaultTreeNode(new Document("Chevron Products - 1072381", "-", "Pages Document", "AMSS", "-", "-", "-"), ru1015);
        chevron.setExpanded(true);
        TreeNode chevron1 = new DefaultTreeNode(new Document("POC-11261", "-", "Pages Document", "AMSS", "15", "10", "8"), chevron);
        TreeNode chevron2 = new DefaultTreeNode(new Document("POC-13361", "-", "Pages Document", "AMSS", "30", "12", "7"), chevron);

        TreeNode phillips = new DefaultTreeNode(new Document("Phillips 66 - 07771205", "-", "Pages Document", "AMSS", "-", "-", "-"), ru1015);
        phillips.setExpanded(true);
        TreeNode phillips1 = new DefaultTreeNode(new Document("POC-88776", "-", "Pages Document", "AMSS", "5", "2", "4"), phillips);
        TreeNode phillips2 = new DefaultTreeNode(new Document("POC-88996", "-", "Pages Document", "AMSS", "21", "7", "6"), phillips);

        //Documents
        //TreeNode expenses = new DefaultTreeNode("document", new Document("Expenses.doc", "30 KB", "Word Document"), work);
        //TreeNode resume = new DefaultTreeNode("document", new Document("Resume.doc", "10 KB", "Word Document"), work);
        //TreeNode refdoc = new DefaultTreeNode("document", new Document("RefDoc.pages", "40 KB", "Pages Document"), primefaces);
        return root;
    }

    public TreeNode createCheckboxDocuments() {
        TreeNode root = new CheckboxTreeNode(new Document("Files", "-", "Folder"), null);

        TreeNode documents = new CheckboxTreeNode(new Document("Documents", "-", "Folder"), root);
        TreeNode pictures = new CheckboxTreeNode(new Document("Pictures", "-", "Folder"), root);
        TreeNode movies = new CheckboxTreeNode(new Document("Movies", "-", "Folder"), root);

        TreeNode work = new CheckboxTreeNode(new Document("Work", "-", "Folder"), documents);
        TreeNode primefaces = new CheckboxTreeNode(new Document("PrimeFaces", "-", "Folder"), documents);

        //Documents
        TreeNode expenses = new CheckboxTreeNode("document", new Document("Expenses.doc", "30 KB", "Word Document"), work);
        TreeNode resume = new CheckboxTreeNode("document", new Document("Resume.doc", "10 KB", "Word Document"), work);
        TreeNode refdoc = new CheckboxTreeNode("document", new Document("RefDoc.pages", "40 KB", "Pages Document"), primefaces);

        //Pictures
        TreeNode barca = new CheckboxTreeNode("picture", new Document("barcelona.jpg", "30 KB", "JPEG Image"), pictures);
        TreeNode primelogo = new CheckboxTreeNode("picture", new Document("logo.jpg", "45 KB", "JPEG Image"), pictures);
        TreeNode optimus = new CheckboxTreeNode("picture", new Document("optimusprime.png", "96 KB", "PNG Image"), pictures);

        //Movies
        TreeNode pacino = new CheckboxTreeNode(new Document("Al Pacino", "-", "Folder"), movies);
        TreeNode deniro = new CheckboxTreeNode(new Document("Robert De Niro", "-", "Folder"), movies);

        TreeNode scarface = new CheckboxTreeNode("movie", new Document("Scarface", "15 GB", "Movie File"), pacino);
        TreeNode carlitosWay = new CheckboxTreeNode("movie", new Document("Carlitos' Way", "24 GB", "Movie File"), pacino);

        TreeNode goodfellas = new CheckboxTreeNode("movie", new Document("Goodfellas", "23 GB", "Movie File"), deniro);
        TreeNode untouchables = new CheckboxTreeNode("movie", new Document("Untouchables", "17 GB", "Movie File"), deniro);

        return root;
    }
}
