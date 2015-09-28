package org.pitest.report;

import org.jsoup.nodes.Element;

public class CoverageBody {
    private int numberOfClasses,
            lineCoverageprocentage,
            lineCoverageActual,
            lineCoverageTotal,
            mutationCoverageprocentage,
            mutationCoverageActual,
            mutationCoverageTotal;

    public CoverageBody() {

    }

    public CoverageBody(Element element) {
        //numberOfClasses
        Element tdNumberOfClasses = element.getElementsByTag("td").get(0);
        numberOfClasses = Integer.parseInt(tdNumberOfClasses.html());

        //Line Coverage
        Element tdCoverageLine = element.getElementsByTag("td").get(1);
        Element coverageLedgendLine = tdCoverageLine.getElementsByClass("coverage_ledgend").get(0);
        lineCoverageActual = Integer.parseInt(coverageLedgendLine.html().split("/")[0]);
        lineCoverageTotal = Integer.parseInt(coverageLedgendLine.html().split("/")[1]);

        //Line Coverage
        Element tdCoverageMut = element.getElementsByTag("td").get(2);
        Element coverageLedgendMut = tdCoverageMut.getElementsByClass("coverage_ledgend").get(0);
        mutationCoverageActual = Integer.parseInt(coverageLedgendMut.html().split("/")[0]);
        mutationCoverageTotal = Integer.parseInt(coverageLedgendMut.html().split("/")[1]);
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public int getLineCoverageActual() {
        return lineCoverageActual;
    }


    public int getLineCoverageTotal() {
        return lineCoverageTotal;
    }

    public int getMutationCoverageActual() {
        return mutationCoverageActual;
    }


    public int getMutationCoverageTotal() {
        return mutationCoverageTotal;
    }

    public int getLineCoverageprocentage() {
        return lineCoverageprocentage;
    }


    public int getMutationCoverageprocentage() {
        return mutationCoverageprocentage;
    }

    public void append(CoverageBody coverageBodyChild) {
        this.numberOfClasses += coverageBodyChild.numberOfClasses;
        this.lineCoverageActual += coverageBodyChild.lineCoverageActual;
        this.lineCoverageTotal += coverageBodyChild.lineCoverageTotal;
        this.mutationCoverageActual += coverageBodyChild.mutationCoverageActual;
        this.mutationCoverageTotal += coverageBodyChild.mutationCoverageTotal;

        this.lineCoverageprocentage = Math.round((this.getLineCoverageActual() * 100) / this.getLineCoverageTotal());
        this.mutationCoverageprocentage = Math.round((this.getMutationCoverageActual() * 100) / this.getMutationCoverageTotal());
    }
}