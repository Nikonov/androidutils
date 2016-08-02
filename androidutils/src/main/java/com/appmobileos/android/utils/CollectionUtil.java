package com.appmobileos.android.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.spbtv.tele2.models.app.ChannelItem;
import com.spbtv.tele2.models.app.EpgItem;
import com.spbtv.tele2.models.app.GenreIviHolder;
import com.spbtv.tele2.models.app.HttpParam;
import com.spbtv.tele2.models.app.Indent;
import com.spbtv.tele2.models.app.ServiceContent;
import com.spbtv.tele2.models.app.filter.FilterItemImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Andrey Nikonov on 22.03.16.
 * Helpers method for work with collection
 */
public class CollectionUtil {
    /**
     * Checks collection
     *
     * @return true if collection null or empty
     */
    public static <T> boolean isCollectionNullOrEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks collection
     *
     * @return true if collection null or empty
     */
    public static <T> List<T> getNullableList(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    /**
     * Convert array to pattern: item,item1,item2 e.t.
     *
     * @param array array which need convert
     * @return empty string or string format item,item1,item2 e.t.
     */
    public static String convertArrayToString(String[] array) {
        if (array == null || array.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        final String separator = ",";
        for (int i = 0; i < array.length; i++) {
            boolean isLastItem = (i == array.length - 1);
            builder.append(array[i]);
            if (!isLastItem) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    /**
     * Convert list params to map {@link HttpParam#getName()} - key in map,
     * {@link HttpParam#getValue()} - value in map
     *
     * @param params list http params
     * @return map with row where key {@link HttpParam#getName()} and value {@link HttpParam#getValue()}
     */
    public static Map<String, String> convertHttpParamsToMap(List<HttpParam> params) {
        if (params == null || params.size() == 0) return new HashMap<>();
        Map<String, String> mapParams = new HashMap<>(params.size());
        for (HttpParam param : params) {
            mapParams.put(param.getName(), param.getValue());
        }
        return mapParams;
    }

    /**
     * Sort collection by {@link Indent#getStatus()}.
     * First - {@link Indent#STATUS_ON},
     * second - {@link Indent#STATUS_SUSPEND},
     * third - {@link Indent#isWaitingStatus(String)}
     * fourth - otherwise
     *
     * @return sorted list or empty if had problem
     */
    public static List<Indent> sortByStatus(List<Indent> include) {
        if (isCollectionNullOrEmpty(include)) return new ArrayList<>();
        Function<Indent, Integer> assignWeights = new Function<Indent, Integer>() {
            @Override
            public Integer apply(Indent from) {
                if (from.isStatusOn()) {
                    return 0;
                } else if (from.isStatusSuspend()) {
                    return 1;
                } else if (from.isWaitingStatus(from.getStatus())) {
                    return 2;
                }
                return 3;
            }
        };
        return Ordering.natural().onResultOf(assignWeights).sortedCopy(include);
    }

    /**
     * Sort collection by {@link GenreIviHolder#getTitleFromGenre()} .
     *
     * @return sorted list or empty if had problem
     */
    public static List<GenreIviHolder> sortByTitle(List<GenreIviHolder> include) {
        if (isCollectionNullOrEmpty(include)) return new ArrayList<>();
        Ordering<GenreIviHolder> byTitle = Ordering.natural().onResultOf(
                new Function<GenreIviHolder, String>() {
                    public String apply(GenreIviHolder genre) {
                        return genre.getTitleFromGenre();
                    }
                });
        return byTitle.sortedCopy(include);
    }

    /**
     * Sort by conversion design (UI)
     *
     * @return sorted list or empty if had problem
     */
    public static List<FilterItemImpl> sortFiltersByDesign(List<FilterItemImpl> include) {
        if (isCollectionNullOrEmpty(include)) return new ArrayList<>();
        Function<FilterItemImpl, Integer> assignWeights = new Function<FilterItemImpl, Integer>() {
            @Override
            public Integer apply(FilterItemImpl from) {
                if (FiltersUtils.isFilterCategory(from)) {
                    return 0;
                } else if (FiltersUtils.isFilterGenre(from)) {
                    return 1;
                } else if (FiltersUtils.isFilterCountry(from)) {
                    return 2;
                } else if (FiltersUtils.isFilterYear(from)) {
                    return 3;
                } else if (FiltersUtils.isFilterSort(from)) {
                    return 4;
                }
                return 5;
            }
        };
        return Ordering.natural().onResultOf(assignWeights).sortedCopy(include);
    }

    public static List<EpgItem> getSortedEpgByRating(List<EpgItem> source) {
        return Ordering.natural().onResultOf(new Function<EpgItem, Comparable>() {
            @Nullable
            @Override
            public Comparable apply(@Nullable EpgItem input) {
                return input.getLiveRating();
            }
        }).reverse().immutableSortedCopy(source);
    }

    public static void collectEpgRating(List<EpgItem> epgItems, Map<Integer, Double> ratings) {
        for (EpgItem item : epgItems) {
            Double rating = ratings.get(item.getProgramId());
            if (rating != null) {
                item.setLiveRating(rating);
            }

        }
    }

    public static List<ChannelItem> getChannelsWithEpg(List<EpgItem> epgItems, List<ChannelItem> channels) {
        ArrayList<ChannelItem> result = new ArrayList<>();
        for (EpgItem epgItem : epgItems) {
            for (ChannelItem channel : channels) {
                if (channel.getId().equals(epgItem.getChannelId())) {
                    channel.setEpgs(Lists.newArrayList(epgItem));
                    result.add(channel);
                }
            }
        }
        return result;
    }

    public static Predicate<ServiceContent> createChannelPredicante(String input) {
        final String channelId = Strings.nullToEmpty(input);
        return new Predicate<ServiceContent>() {
            @Override
            public boolean apply(@Nullable ServiceContent input) {
                List<String> channelIds = input != null ? input.getTvIds() : null;
                return !isCollectionNullOrEmpty(channelIds) && channelIds.contains(channelId);
            }
        };
    }

}
