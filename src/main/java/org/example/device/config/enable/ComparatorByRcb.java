package org.example.device.config.enable;

import com.beanit.iec61850bean.Rcb;

import java.util.Comparator;

public class ComparatorByRcb implements Comparator<Rcb> {

        @Override
        public int compare(Rcb o1, Rcb o2) {
            String s1 = o1.getReference().toString();
            String s2 = o2.getReference().toString();
            if (s1.equals(s2)) {
                return 0;
            }
            return s1.compareTo(s2);
        }
    }