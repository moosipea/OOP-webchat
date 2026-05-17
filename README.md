# OOP rühmatöö

Online chat rakendus (server + klient).

## I etapi lõpuks
On lihtne UI, teksti vormindamine ja lihtne võrgusuhtlus.

## II etapiks (meil ei ole eriti plaaji ja on lihtsalt funktsioonid, mida me tahame implementeerida)
krüpteeritud suhtlus ja kasutajad (koos registreerimise ja sisse logimisega)

## Kuidas käivitada
 - Jooksuta `mvn clean package` projekti juurkaustas.
 - Genereeri TLS võtmed kasutades skripti `generate_keys.sh`, kus paroolideks pane `123456`. Lõpus kui küsib, kas usaldada sertifikaati, kirjuta `yes`. Muu info võid sisestada nagu tahad.
 - Serveri saad käivitada nii: `java -jar server/target/server-1.0-SNAPSHOT.jar`.
 - Kliendi saad käivitada nii: `java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar`.
 - NB! Pane tähele, et sertifikaat ja vastavad võtmed oleksid nähtavad jooksutatavale programmile!

## Liikmed
Oskar Austa (github: oskarasd123) <br>
Mattias Volt (github: moosipea) <br>

## Põhifunktsionaalsused (peab kindlasti olema):
- [x] reaalajas sõnumivahetus
	- [x] kasutajalt kasutajale
    - [x] kasutajalt kanalisse
- [x] sõnumite salvestamine
	- [x] serveris andmebaas   
	- [x] ühendades küsib klient serverilt sõnumeid, mis vahepeal saadetud on


## Kõrvalfunktsionaalsused (võiks olla ka):

- [x] **lihtne graafiline UI kliendile**
- [x] kasutajate autentimine, salasõnaga ühendamine
- [ ] sõnumite otsimine, filtreerimine
- [ ] sõnumite editimine ja kustutamine (editimisel vastav märge)
- [ ]  grupichatid
    - [ ] kasutajate privileegid
    - [x] Kliendi ja serveri käsud (nagu /dm, /motd vms)
    
- [ ] visuaalsed elemendid
	- [x] sõnumite formatting ja renderdamine (Markdown või selle laadne)
	- [ ] kasutajate pingimine @kasutajanimi süntaksiga 
	- [x] kasutajatele on määratud värv (kasutajanime järgi)
	- [ ] võimalik on näha teiste kasutajate online-staatust
- [x] turvaline ühendus
- [x] programmerimiskeelst sõötumatu suhtlusprotokoll
