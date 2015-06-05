package ee.tkasekamp.vickywaranalyzer.service;

import ee.tkasekamp.vickywaranalyzer.core.Country;
import ee.tkasekamp.vickywaranalyzer.core.JoinedCountry;
import ee.tkasekamp.vickywaranalyzer.core.War;
import ee.tkasekamp.vickywaranalyzer.parser.Parser;
import ee.tkasekamp.vickywaranalyzer.util.Localisation;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.*;

public class ModelServiceImpl implements ModelService {
	private String date = "";
	private String player = "";
	private String startDate = "";
	private ArrayList<Country> countryList;
	private ArrayList<War> warList;

	private UtilService utilServ;
	private Parser parser;

	public ModelServiceImpl(UtilService utilServ) {
		this.utilServ = utilServ;
		parser = new Parser(this);
		countryList = new ArrayList<>();
	}

	@Override
	public String createModel(String saveGamePath, boolean useLocalisation) {

		try {
			warList = parser.read(saveGamePath);
		} catch (IOException e1) {
			return "Couldn't read save game";
		}
		/* Generating a list of countries from warList */
		createUniqueCountryList();

		/* Localisation */
		// TODO multithreading
		if (useLocalisation)
			Localisation.readLocalisation(utilServ.getInstallFolder(), countryList);
		/* Finding official names for every country and battle */
		// TODO try to optimise
		for (War war : warList) {
			setWarOfficialNames(war);
		}
		// TODO multithreading
		try {
			utilServ.writePathsToFile();
		} catch (IOException e) {
			return "Couldn't write to file";
		}
		// TODO multithreading
		for (Country country : countryList) {
			country.setFlag(utilServ.loadFlag(country.getTag()));
		}
		return "Everything is OK";
	}

	@Override
	public void reset() {
		date = "";
		player = "";
		startDate = "";
		countryList.clear();
	}

	@Override
	public String getOfficialName(String tag) {
		for (Country country : countryList) {
			if (country.getTag().equals(tag)) {
				return country.getOfficialName();
			}
		}
		return tag;

	}

	private void setWarOfficialNames(War war) {
		war.setOfficialNames(getOfficialName(war.getOriginalAttacker()),
				getOfficialName(war.getOriginalDefender()));
	}

	/**
	 * Creating a list that will contain every unique country.
	 */
	private void createUniqueCountryList() {
		// TODO try to optimise
		/* Adding all countries to a set to get only unique ones */
		Set<String> set = new HashSet<>();
		for (War item : warList) {
			for (JoinedCountry country : item.getCountryList()) {
				set.add(country.getTag());
			}
		}
		/* This list is used to arrange the countries alphabetically */
		List<String> tempcountrylist = new ArrayList<>();
		/* From set to list */
		for (String strin : set) {
			tempcountrylist.add(strin);
		}
		Collections.sort(tempcountrylist);

		// Adding to countryList
		for (String tag : tempcountrylist) {
			countryList.add(new Country(tag));
		}

	}

	@Override
	public void setDate(String line) {
		this.date = line;

	}

	@Override
	public void setPlayer(String line) {
		this.player = line;

	}

	@Override
	public void setStartDate(String line) {
		this.startDate = line;

	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getPlayer() {
		return player;
	}

	@Override
	public String getStartDate() {
		return startDate;
	}

	@Override
	public String getPlayerOfficial() {
		return getOfficialName(player);
	}

	@Override
	public ArrayList<Country> getCountries() {
		return countryList;
	}

	@Override
	public ArrayList<War> getWars() {
		return warList;
	}

	@Override
	public boolean isHOD() {
		for (War war : warList) {
			if (!(war.getOriginalWarGoal().getCasus_belli().equals(""))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Image getFlag(String tag) {
		for (Country country : countryList) {
			if (country.getTag().equals(tag)) {
				return country.getFlag();
			}
		}
		return null;
	}

}