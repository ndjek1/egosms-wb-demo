/*
   Copyright 2009-2021 PrimeTek.

   Licensed under PrimeFaces Commercial License, Version 1.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   Licensed under PrimeFaces Commercial License, Version 1.0 (the "License");

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pahappa.systems.california.settings;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "guestPreferences")
@SessionScoped
public class CaliforniaSettings implements Serializable {

    private String layout = "moody";

    private String theme = "blue";

    private String menuColor = "light";

    private String megaMenuColor = "gradient";

    private String menuLayout = "static";

    private String profileMode = "inline";

    private String inputStyle = "outlined";

    private List<Theme> themes;

    private List<SpecialTheme> specialThemes;

    @PostConstruct
    public void init() {
        themes = new ArrayList<>();
        themes.add(new Theme("Blue", "blue", "#0071bc"));
        themes.add(new Theme("Cyan", "cyan", "#00bfe7"));
        themes.add(new Theme("Green", "green", "#4AA564"));
        themes.add(new Theme("Yellow", "yellow", "#F9C642"));
        themes.add(new Theme("Purple", "purple", "#6A4AA5"));
        themes.add(new Theme("Pink", "pink", "#9f4488"));
        themes.add(new Theme("Blue-Grey", "bluegrey", "#4B6D7E"));
        themes.add(new Theme("Teal", "teal", "#07A089"));
        themes.add(new Theme("Orange", "orange", "#fe875d"));
        themes.add(new Theme("Grey", "grey", "#5B616B"));

        specialThemes = new ArrayList<>();
        specialThemes.add(new SpecialTheme("Cappuccino", "cappuccino", "cappuccino"));
        specialThemes.add(new SpecialTheme("Montreal", "montreal", "montreal"));
        specialThemes.add(new SpecialTheme("Hollywood", "hollywood", "hollywood"));
        specialThemes.add(new SpecialTheme("Peak", "peak", "peak"));
        specialThemes.add(new SpecialTheme("Alive", "alive", "#CB356B","#BD3F32"));
        specialThemes.add(new SpecialTheme("Emerald", "emerald", "#348F50","#56B4D3"));
        specialThemes.add(new SpecialTheme("Ash", "ash", "#606c88","#3f4c6b"));
        specialThemes.add(new SpecialTheme("Noir", "noir", "#4b6cb7","#182848"));
        specialThemes.add(new SpecialTheme("Mantle", "mantle", "#514A9D","#24C6DC"));
        specialThemes.add(new SpecialTheme("Predawn", "predawn", "#00223E","#FFA17F"));
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public List<SpecialTheme> getSpecialThemes() {
        return specialThemes;
    }

    public String getInputStyleClass() {
        return this.inputStyle.equals("filled") ? "ui-input-filled" : "";
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }

    public String getMenuColor() {
        return menuColor;
    }

    public void setMenuColor(String menuColor) {
        this.menuColor = menuColor;
    }

    public String getMegaMenuColor() {
        return megaMenuColor;
    }

    public void setMegaMenuColor(String megaMenuColor) {
        this.megaMenuColor = megaMenuColor;
    }

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

    public String getMenuLayout() {
        return menuLayout;
    }

    public void setMenuLayout(String menuLayout) {
        this.menuLayout = menuLayout;

        if (this.menuLayout.equals("layout-wrapper-horizontal-sidebar")) {
            this.profileMode = "topbar";
        }
    }

    public String getProfileMode() {
        return profileMode;
    }

    public void setProfileMode(String profileMode) {
        if (this.menuLayout.equals("layout-wrapper-horizontal-sidebar")) {
            this.profileMode = "topbar";
        }
        else {
            this.profileMode = profileMode;
        }
    }

    public class Theme {

        private String name;
        private String file;
        private String color;

        public Theme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    public class SpecialTheme {

        private String name;
        private String file;
        private String image;
        private String color1;
        private String color2;

        public SpecialTheme(String name, String file, String color1, String color2) {
            this.name = name;
            this.file = file;
            this.color1 = color1;
            this.color2 = color2;
        }

        public SpecialTheme(String name, String file, String image) {
            this.name = name;
            this.file = file;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getColor1() {
            return color1;
        }

        public void setColor1(String color1) {
            this.color1 = color1;
        }

        public String getColor2() {
            return color2;
        }

        public void setColor2(String color2) {
            this.color2 = color2;
        }
    }
}
