package com.starter.nova.common.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tql
 */
@UtilityClass
public class StreamUtil {

    public static <T> Stream<T> of(Collection<T> list) {
        return Optional.ofNullable(list).map(Collection::stream).orElseGet(Stream::empty);
    }

    public static <T, R> Map<T, R> of(Map<T, R> map) {
        return Optional.ofNullable(map).orElseGet(Collections::emptyMap);
    }

    public static <T, R> Stream<R> of(Collection<T> list, Function<? super T, ? extends R> mapFunc) {
        return Optional.ofNullable(list).map(Collection::stream).orElseGet(Stream::empty).map(mapFunc);
    }

    public static <T> Stream<T> ofByFilter(Collection<T> list, Predicate<T> filter) {
        return Optional.ofNullable(list).map(Collection::stream).orElseGet(Stream::empty).filter(filter);
    }

    public static <T> T findFirst(Collection<T> list) {
        return StreamUtil.of(list).findFirst().orElse(null);
    }

    public static <T, R> R findFirst(Collection<T> list, Function<? super T, ? extends R> mapFunc) {
        return StreamUtil.of(list).map(mapFunc).findFirst().orElse(null);
    }

    public static <T, R> R findFirst(T t, Function<? super T, ? extends R> mapFunc) {
        return Optional.ofNullable(t).map(mapFunc).orElse(null);
    }

    public static <T, R> List<R> findList(T t, Function<? super T, List<R>> mapFunc) {
        return Optional.ofNullable(t).map(mapFunc).orElse(Collections.emptyList());
    }

    public static <T> T findFirstByFilter(Collection<T> list, Predicate<T> filter) {
        return StreamUtil.of(list).filter(filter).findFirst().orElse(null);
    }

    public static <T, R> R findFirstByFilter(Collection<T> list, Predicate<T> filter,
                                             Function<? super T, ? extends R> mapFunc) {
        return StreamUtil.of(list).filter(filter).map(mapFunc).findFirst().orElse(null);
    }

    public static <T> List<T> toList(Collection<T> list) {
        return StreamUtil.of(list).collect(Collectors.toList());
    }

    public static <T> List<T> toListByFilter(Collection<T> list, Predicate<T> filter) {
        return StreamUtil.of(list).filter(filter).collect(Collectors.toList());
    }

    public static <T, R> Set<R> toSet(Collection<T> list, Function<? super T, R> mapFunc) {
        return StreamUtil.of(list).filter(Objects::nonNull).map(mapFunc).collect(Collectors.toSet());
    }

    public static <T, R> Set<R> toSet(Collection<T> list, Predicate<? super T> predicate, Function<? super T, R> mapFunc) {
        return StreamUtil.of(list).filter(Objects::nonNull).filter(predicate).map(mapFunc).collect(Collectors.toSet());
    }

    public static <T> void executeAsync(List<T> items, Consumer<T> action, ExecutorService threadPool) {
        List<CompletableFuture<Void>> futureTasks = items.stream()
                .map(item -> CompletableFuture.runAsync(() -> action.accept(item), threadPool))
                .toList();

        futureTasks.forEach(CompletableFuture::join);
    }

    public static <T, R> List<R> toListAsync(List<T> items, Function<T, R> function, ExecutorService threadPool) {
        List<CompletableFuture<R>> futureTasks = StreamUtil.of(items)
                .map(item -> CompletableFuture.supplyAsync(() -> function.apply(item), threadPool))
                .toList();
        return futureTasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public static <T, R> Map<R, T> toMap(List<T> list, Function<? super T, ? extends R> keyMapFunc) {
        return of(list).collect(Collectors.toMap(keyMapFunc, item -> item));
    }
}
