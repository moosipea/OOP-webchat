# OOP rühmatöö plaan

Online chat rakendus (server + klient).

## I etapi lõpuks
On lihtne UI, teksti vormindamine ja lihtne võrgusuhtlus.

## II etapiks (meil ei ole eriti plaaji ja on lihtsalt funktsioonid, mida me tahame implementeerida)
krüpteeritud suhtlus ja kasutajad (koos registreerimise ja sisse logimisega)

## Mida veel teha
sõnumite salvestamine ja ajaloo haldamine.

## Kuidas jooksutada
 - Alguses jooksuta `mvn clean install` projekti juurkaustas.
 - Genereeri TLS võtmed kasutades `generate_keys.sh`, kus paroolideks pane `123456`. Lõpus kui küsib, kas usaldada sertifikaati kirjuta `yes`. Muu info võid sisestada nagu tahad.
 - Käivita server (intellij/code kaudu või buildi maveniga ja käivita jar), seejärel jooksuta klient kasutades `mvn javafx:run` peale client kausta minemist. Lokaalse serveriga ühendades võib ip ja port väljad tühjaks jätta (need täidetakse vaikeväärtustega).

## Liikmed
Oskar Austa (github: oskarasd123) <br>
Mattias Volt (github: moosipea) <br>

## Põhifunktsionaalsused (peab kindlasti olema):
-   reaalajas sõnumivahetus
	-   kasutajalt kasutajale
	-   kasutajalt kanalisse
-   sõnumite salvestamine
	-   serveris andmebaas   
	-   kliendis andmebaas
	-   ühendades küsib klient serverilt sõnumeid, mis vahepeal saadetud on



## Kõrvalfunktsionaalsused (võiks olla ka):

**lihtne graafiline UI kliendile**

-   kasutajate autentimine, salasõnaga ühendamine
-   sõnumite otsimine, filtreerimine
-   sõnumite editimine ja kustutamine (editimisel vastav märge)
-   grupichatid
	-   kasutajate privileegid
	-   Kliendi ja serveri käsud (nagu /dm, /motd vms)
    
-   visuaalsed elemendid
	-   sõnumite formatting ja renderdamine (Markdown või selle laadne)
	-   kasutajate pingimine @kasutajanimi süntaksiga 
	-   kasutajatele on määratud värv (kasutajanime järgi)
	-   võimalik on näha teiste kasutajate online-staatust
    

**Turvalisus**

-   ideaalis tahaks, et ühendus oleks võimalikult turvaline
-   suhtlusprotokoll võiks olla selline, et sama hästi on kasutatav GUI klient ja command-line klient.
	-   projekti raames teeme ennekõike GUI kliendi
	-   kui aega jääb, võib ka command-line kliendi teha
