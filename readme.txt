==================================================
            Projekt IJA - Hra LightBulb
==================================================

Popis projektu:
----------------
Tento projekt je semestrální prací pro předmět IJA na VUT FIT.
Jedná se o logickou hru inspirovanou hrou "LightBulb" pro Android. Cílem hry je otáčet jednotlivými políčky na herní desce tak, aby se propojily všechny vodiče od zdroje energie ke všem žárovkám a tím je rozsvítily.

Autor:
-------
- Gleb Litvinchuk (xlitvi02)

Použité technologie:
---------------------
- Java SE 21
- JavaFX pro grafické uživatelské rozhraní
- Maven pro správu projektu a závislostí

Struktura projektu:
--------------------
Projekt dodržuje standardní adresářovou strukturu Maven:
- `src/main/java/`: Zdrojové soubory Java (balíčky `lightbulb.controller`, `lightbulb.log`, `lightbulb.model`, `lightbulb.view`)
- `src/main/resources/`: Zdroje aplikace
    - `img/`: Obrázky pro herní prvky (rozdělené do podadresářů dle skinů, např. `standart/`, `oil/`)
    - `maps/`: Předpřipravené herní úrovně ve formátu JSON.
    - `styles/`: CSS soubory pro témata vzhledu (light-theme.css, dark-theme.css).
    - `icon.png`: Ikona aplikace.
- `pom.xml`: Konfigurační soubor Maven.

Příprava a překlad projektu:
-----------------------------
1.  Ujistěte se, že máte nainstalovanou Java JDK verze 21 nebo novější.
2.  Ujistěte se, že máte nainstalovaný Apache Maven.
3.  Otevřete terminál nebo příkazový řádek.
4.  Přejděte do kořenového adresáře projektu (kde se nachází soubor `pom.xml`).
5.  Pro překlad a sestavení aplikace spusťte příkaz:
    ```bash
    mvn clean package
    ```
    Tento příkaz zkompiluje zdrojové kódy a vytvoří spustitelný balíček aplikace (obvykle v adresáři `target/`).

    Alternativně, pokud chcete také vygenerovat Javadoc dokumentaci (za předpokladu, že je `maven-javadoc-plugin` nakonfigurován v `pom.xml`):
    ```bash
    mvn clean package javadoc:javadoc
    ```
    Dokumentace bude vygenerována do adresáře `target/site/apidocs/`.


Spuštění aplikace:
-------------------
Aplikaci lze spustit několika způsoby:

1.  **Pomocí Maven JavaFX pluginu (doporučeno pro vývoj):**
    V kořenovém adresáři projektu spusťte:
    ```bash
    mvn javafx:run
    ```

2.  **Spuštěním vytvořeného JAR archivu:**
    Aplikaci pak můžete spustit příkazem:
    ```bash
    java -jar target/demo-1.0-SNAPSHOT-shaded.jar
    ```

Základní vlastnosti implementované aplikace:
-------------------------------------------
- Generování herních úrovní různých obtížností (Easy, Medium, Hard).
- Herní mechanika otáčení prvků pro spojení elektrického obvodu.
- Vizuální rozlišení napájených a nenapájených prvků.
- Časový limit pro obtížnost Hard.
- Možnost zobrazení nápovědy (počet otáček do správné pozice).
- Zobrazení statistik na konci hry.
- Logování průběhu hry do souboru a možnost jeho zpětného přehrání.
- Funkce Undo/Redo.
- Ukládání aktuálně vygenerované úrovně.
- Výběr předdefinovaných úrovní.
- Možnost změny vzhledu (skiny prvků) a tématu (světlé/tmavé).