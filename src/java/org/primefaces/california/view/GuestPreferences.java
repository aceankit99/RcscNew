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
package org.primefaces.california.view;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class GuestPreferences implements Serializable {

	private String layout = "moody";

	private String theme = "blue";

	private boolean darkMenu = true;

	private boolean gradientMenu = false;

	private boolean darkMegaMenu = true;

	private boolean gradientMegaMenu = false;

	private String menuLayout = "static";

    private String profileMode = "inline";

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public boolean isDarkMenu() {
		return this.darkMenu;
	}

	public boolean isGradientMenu() {
		return this.gradientMenu;
	}

	public boolean isDarkMegaMenu() {
		return this.darkMegaMenu;
	}

	public boolean isGradientMegaMenu() {
		return this.gradientMegaMenu;
	}

	public void setMenuMode(boolean dark, boolean gradient, String theme) {
		this.darkMenu = dark;
		this.gradientMenu = gradient;
		this.theme = theme;
	}

	public void setMegaMenuMode(boolean dark, boolean gradient) {
		this.darkMegaMenu = dark;
		this.gradientMegaMenu = gradient;
	}

	public String getMenuLayout() {
		return menuLayout;
	}

	public void setMenuLayout(String menuLayout) {
		this.menuLayout = menuLayout;
	}

    public String getProfileMode() {
        return profileMode;
    }

    public void setProfileMode(String profileMode) {
        this.profileMode = profileMode;
    }
}
