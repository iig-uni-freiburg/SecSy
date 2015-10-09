package de.uni.freiburg.iig.telematik.secsy.logic.generator.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.invation.code.toval.types.HashList;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntryUtils;

public class SimulationLogEntryUtils extends LogEntryUtils {

        public static <E extends SimulationLogEntry> List<E> getEntriesWithAlternativeOriginator(List<E> entries) {
                validateEntries(entries);
                List<E> entriesWithAlternativeOriginator = new ArrayList<>();
                for (E entry : entries) {
                        if (!entry.isFieldLocked(EntryField.ORIGINATOR) && entry.getOriginatorCandidates().size() > 1) {
                                entriesWithAlternativeOriginator.add(entry);
                        }
                }
                return entriesWithAlternativeOriginator;
        }

        public static <E extends SimulationLogEntry> List<E> getEntriesWithNoAlternativeOriginator(List<E> entries) {
                validateEntries(entries);
                List<E> entriesWithNoAlternativeOriginator = new ArrayList<>();
                for (E entry : entries) {
                        if (entry.isFieldLocked(EntryField.ORIGINATOR) || entry.getOriginatorCandidates().size() == 1) {
                                entriesWithNoAlternativeOriginator.add(entry);
                        }
                }
                return entriesWithNoAlternativeOriginator;
        }

        /**
         * Returns the intersection of all sets of originator candidates for the
         * given log entries.<br>
         * That set contains all originators that are candidates for all
         * entries.
         *
         * @param <E>
         * @param entries A set of log entries
         * @return Shared originator candidates (shuffled)
         */
        public static <E extends SimulationLogEntry> List<String> getSharedOriginatorCandidates(List<E> entries) {
                validateEntries(entries);
                List<String> sharedCandidates = new HashList<>();
                sharedCandidates.addAll(entries.get(0).getOriginatorCandidates());
                for (int i = 1; i < entries.size(); i++) {
                        sharedCandidates.retainAll(entries.get(i).getOriginatorCandidates());
                }
                Collections.shuffle(sharedCandidates);
                return sharedCandidates;
        }
}
