library("ggplot2")
library("gridExtra")

configurePlot <- function(plot, brks = 10**seq(0, 12, by=3)) {
  result = plot
  result = result + geom_point(aes(shape=procWeight, col=procWeight), size=2)
  result = result + geom_line(aes(col=procWeight))
  #result = result +
  #  labs(
  #    title = NULL,
  #    subtitle = NULL,
  #    y = "Number of Queries",
  #    x = "Counterexample Length",
  #    caption = NULL
  #  )
  #result = result + scale_x_continuous(breaks=seq(0, 500, 25))
  result = result + scale_x_continuous(trans='log10', breaks=10**seq(0, 12, by=3))
  #result = result + scale_y_continuous(trans='log10') #+ annotation_logticks(sides = "lr")
  result = result + scale_y_continuous(trans='pseudo_log', breaks=brks) #+ annotation_logticks(sides = "lr")
  result = result + theme_bw()
  result = result + scale_shape_discrete(name="Proc. Weight") + scale_color_discrete(name="Proc. Weight")
  return(result)
}

plotNway <- function(meanData, title, shBrks=10**seq(0, 12, by=3)) {
  gg1 = ggplot(meanData, aes(x = totalSymbols, y = reproductionLength))
  gg1 = configurePlot(gg1)
  gg1 = gg1 + labs(x = "Trace Length", y = "RL", title="Reproduction Length")

  gg2 = ggplot(meanData, aes(x = totalSymbols, y = maximumResourceConsumption))
  gg2 = configurePlot(gg2)
  gg2 = gg2 + labs(x = "Trace Length", y = "MRC", title="Maximum Resource Consumption")

  gg3 = ggplot(meanData, aes(x = totalSymbols, y = localCELength))
  gg3 = configurePlot(gg3)
  gg3 = gg3 + labs(x = "Trace Length", y = "LCEL", title="Local Counterexample Length")

  gg4 = ggplot(meanData, aes(x = totalSymbols, y = stackHeight))
  gg4 = configurePlot(gg4, shBrks)
  gg4 = gg4 + labs(x = "Trace Length", y = "SH", title="Stack Height")

  gg5 = ggplot(meanData, aes(x = totalSymbols, y = numReturns))
  gg5 = configurePlot(gg5)
  gg5 = gg5 + labs(x = "Trace Length", y = "NR", title="Number of Returns")

  grid.arrange(gg1, gg2, gg3, gg4, gg5, ncol=2, nrow=3, top=title)
}

plotSingle <- function(data, suffix, shBrks=10**seq(0, 12, by=3)) {
  
  pdf(paste("rl", suffix, ".pdf", sep=""), width=myWidth, height=myHeight)
  gg = ggplot(data, aes(x = totalSymbols, y = reproductionLength))
  gg = configurePlot(gg)
  gg = gg + labs(x = "Trace Length", y = "RL", title="Reproduction Length")
  plot(gg)
  dev.off()
  
  pdf(paste("mrc", suffix, ".pdf", sep=""), width=myWidth, height=myHeight)
  gg = ggplot(data, aes(x = totalSymbols, y = maximumResourceConsumption))
  gg = configurePlot(gg)
  gg = gg + labs(x = "Trace Length", y = "MRC", title="Maximum Resource Consumption")
  plot(gg)
  dev.off()
  
  pdf(paste("lcel", suffix, ".pdf", sep=""), width=myWidth, height=myHeight)
  gg = ggplot(data, aes(x = totalSymbols, y = localCELength))
  gg = configurePlot(gg)
  gg = gg + labs(x = "Trace Length", y = "LCEL", title="Local Counterexample Length")
  plot(gg)
  dev.off()
  
  pdf(paste("sh", suffix, ".pdf", sep=""), width=myWidth, height=myHeight)
  gg = ggplot(data, aes(x = totalSymbols, y = stackHeight))
  gg = configurePlot(gg, shBrks)
  gg = gg + labs(x = "Trace Length", y = "SH", title="Stack Height")
  plot(gg)
  dev.off()
}

randomCSV = read.csv("./output.csv", strip.white = TRUE)
randomMeanData = aggregate(. ~ procWeight + totalSymbols, randomCSV, mean)
randomMedianData = aggregate(. ~ procWeight + totalSymbols, randomCSV, median)

randomMeanData$procWeight = factor(randomMeanData$procWeight)
randomMedianData$procWeight = factor(randomMedianData$procWeight)

# joined
myWidth = 10
myHeight = 7


pdf("random_avg.pdf", width=myWidth, height=myHeight)
plotNway(randomMeanData, "RANDOM (AVG)")
dev.off()

pdf("random_med.pdf", width=myWidth, height=myHeight)
plotNway(randomMedianData, "RANDOM (MEDIAN)", 10**seq(0, 3, by=1))
dev.off()


# single
myWidth = 6
myHeight = 3

plotSingle(randomMeanData, "_avg")
plotSingle(randomMedianData, "_med", 10**seq(0, 3, by=1))

