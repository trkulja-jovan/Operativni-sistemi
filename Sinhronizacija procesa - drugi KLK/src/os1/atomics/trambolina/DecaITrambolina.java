package os1.atomics.trambolina;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * Na jednom seoskom vašaru, najinteresantnija atrakcija desetogodišnjacima je
 * velika trambolina starog Pere. Stari Pera je dobrog srca i pušta mališane da
 * se besplatno zabavljaju na njoj, na opštu radost njihovih roditelja. Mališa-
 * ni takođe koriste ovo i non-stop skaču na trambolini, silazeći jedino kada
 * se umore od skakanja kako bi se malo odmorili za novu rundu zabave.
 * 
 * Nažalost, i trambolina starog Pere je dosta stara pa ne može izdržati više
 * od 300 kila. Prilikom implementacije rešenja imati ovo u vidu i ne dozvoliti
 * da se trambolina pokida. Potrebno je blokirati mališane koji žele da skaču
 * na trambolini ako bi ukupna težina prešla 300 kila.
 * 
 * Zbog povećane mogućnosti povreda kada više dece skače na trambolini, stari
 * Pera ne dozvoljava da na njoj bude više od 5 dece. Takođe, ne terati mališa-
 * ne da nepotrebno čekaju ako ima mesta na trambolini.
 * 
 * Dodatni deo
 * 
 * Kako su dečaci nestašniji i manje paze da nekog slučajno ne udare tokom ska-
 * kanja, potrebno je odvojiti dečake i devojčice, tj. blokirati ulaz devojči-
 * cama ako na trambolini trenutno skaču dečaci, odnosno dečacima ako je trenu-
 * tno koriste devojčice.
 */
public class DecaITrambolina extends Application {

	protected final NewTrambolina t = new NewTrambolina();

	protected final int MAX_TEZINA = 300;
	protected final int MAX_BR_DECE = 5;

	protected enum Pol {
		MUSKI, ZENSKI;
	}

	@AutoCreate(26)
	protected class Dete extends Thread {

		private final Pol pol = randomElement(Pol.values());
		private final int tezina = randomInt(25, 60);
		
		public int getTezina() {
			return tezina;
		}
		
		public Pol getPol() {
			return pol;
		}

		public Dete() {
			setName(String.format("%4.1f kg", 1.0 * tezina));
			setColor(pol == Pol.MUSKI ? AZURE : ROSE);
		}

		@Override
		protected void step() {
			odmara();
			t.popniDecaka(tezina);
			skace();
			t.spustiDecaka(tezina);
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container van       = box("Клупе").color(OLIVE);
	protected final Container unutra    = box("Трамболина").color(ARMY);
	protected final Container main      = column(van, unutra);
	protected final Operation dete      = init().container(van).name("Дете %d").color(ORANGE);

	protected final Operation odmaranje = duration("7±2").text("Одмара").textAfter("Чека");
	protected final Operation skakanje  = duration("5±2").text("Скаче").container(unutra).update(this::azuriraj);

	protected void odmara() {
		odmaranje.performUninterruptibly();
	}

	protected void skace() {
		skakanje.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		int br = 0;
		double tezina = 0.0;
		for (Dete dete : unutra.getItems(Dete.class)) {
			br += 1;
			tezina += dete.tezina;
		}
		unutra.setText(String.format("%4.2f kg / %d", tezina, br));
		if (tezina > MAX_TEZINA || br > MAX_BR_DECE) {
			unutra.setColor(MAROON);
		} else {
			unutra.setColor(ARMY);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("Деца и трамболина");
	}
}