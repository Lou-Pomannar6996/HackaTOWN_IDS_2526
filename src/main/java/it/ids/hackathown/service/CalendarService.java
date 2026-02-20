package it.ids.hackathown.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

    public List<String> createEvent(List<String> slotPreferiti, List<String> partecipanti) {
        if (slotPreferiti == null || slotPreferiti.isEmpty()) {
            return List.of();
        }
        if (partecipanti == null || partecipanti.isEmpty()) {
            return dedupSlots(slotPreferiti);
        }
        Set<String> disponibili = new LinkedHashSet<>();
        for (String slot : partecipanti) {
            if (slot != null && !slot.isBlank()) {
                disponibili.add(slot.trim());
            }
        }
        List<String> validi = new ArrayList<>();
        for (String slot : slotPreferiti) {
            if (slot == null || slot.isBlank()) {
                continue;
            }
            String normalized = slot.trim();
            if (disponibili.contains(normalized) && !validi.contains(normalized)) {
                validi.add(normalized);
            }
        }
        return List.copyOf(validi);
    }

    private List<String> dedupSlots(List<String> slotPreferiti) {
        Set<String> unique = new LinkedHashSet<>();
        for (String slot : slotPreferiti) {
            if (slot != null && !slot.isBlank()) {
                unique.add(slot.trim());
            }
        }
        return List.copyOf(unique);
    }
}
