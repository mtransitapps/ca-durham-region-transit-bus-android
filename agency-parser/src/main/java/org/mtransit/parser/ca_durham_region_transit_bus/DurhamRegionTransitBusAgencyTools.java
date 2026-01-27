package org.mtransit.parser.ca_durham_region_transit_bus;

import static org.mtransit.commons.Constants.EMPTY;
import static org.mtransit.commons.Constants.SPACE_;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirection;

import java.util.regex.Pattern;

// http://opendata.durham.ca/
// https://maps.durham.ca/OpenDataGTFS/GTFS_Durham_TXT.zip
// http://opendata.durham.ca/datasets?t=Durham%20Transportation
// https://maps.durham.ca/OpenDataGTFS/GTFS_Durham_TXT.zip
public class DurhamRegionTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new DurhamRegionTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		if (gRoute.getRouteShortName().startsWith("copy of")) {
			return EXCLUDE;
		}
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean cleanMergedServiceIds() {
		return true;
	}

	@Override
	public boolean cleanMergedRouteIds() {
		return true;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // used by GTFS-RT
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		//noinspection DiscouragedApi
		return cleanRouteOriginalId(gRoute.getRouteId()); // used by GTFS-RT
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_LETTER = Pattern.compile("(^[a-z] )", Pattern.CASE_INSENSITIVE);

	@Nullable
	@Override
	public String selectDirectionHeadSign(@Nullable String headSign1, @Nullable String headSign2) {
		if (StringUtils.equals(headSign1, headSign2)) {
			return null; // can NOT select
		}
		final boolean startsWithLetter1 = headSign1 != null && STARTS_WITH_LETTER.matcher(headSign1).find();
		final boolean startsWithLetter2 = headSign2 != null && STARTS_WITH_LETTER.matcher(headSign2).find();
		if (startsWithLetter1) {
			if (!startsWithLetter2) {
				return headSign2;
			}
		} else if (startsWithLetter2) {
			return headSign1;
		}
		return null;
	}

	@Nullable
	@Override
	public String mergeComplexDirectionHeadSign(@Nullable String headSign1, @Nullable String headSign2) {
		if (StringUtils.equals(headSign1, headSign2)) {
			return null; // can NOT select
		}
		if (headSign1 == null || headSign2 == null) {
			return null;
		}
		final boolean startsWithLetter1 = STARTS_WITH_LETTER.matcher(headSign1).find();
		final boolean startsWithLetter2 = STARTS_WITH_LETTER.matcher(headSign2).find();
		if (startsWithLetter1 && startsWithLetter2) {
			return MDirection.mergeHeadsignValue(
					headSign1.substring(2),
					headSign2.substring(2)
			);
		}
		return null;
	}

	private static final Pattern DASH_ = Pattern.compile("( - | -|- )", Pattern.CASE_INSENSITIVE);

	private static final Pattern START_WITH_RSN = Pattern.compile("(^\\d+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_LETTER_ = Pattern.compile("(^([A-Z])\\s?-\\s?)");
	private static final String STARTS_WITH_LETTER_REPLACEMENT = "$2 ";

	private static final Pattern LATE_NIGHT_SHUTTLE_ = CleanUtils.cleanWords("late night shuttle");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = DASH_.matcher(tripHeadsign).replaceAll(SPACE_);
		tripHeadsign = START_WITH_RSN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STARTS_WITH_LETTER_.matcher(tripHeadsign).replaceAll(STARTS_WITH_LETTER_REPLACEMENT);
		tripHeadsign = LATE_NIGHT_SHUTTLE_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(getFirstLanguageNN(), tripHeadsign);
	}

	@NotNull
	private String[] getIgnoredWords() {
		return new String[]{
				"OPG", "UOIT",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), gStopName, getIgnoredWords());
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(getFirstLanguageNN(), gStopName);
	}

	@Override
	public boolean cleanMergedStopIds() {
		return true;
	}
}
