# Settings for the plots
colours = list(
  red 	= "#AF3547",
  blue   	= "#0074BC",
  black  = "#000000",
  grey = "#777777"
)
font.size = 10;
font.family ="Garamond";

tex.textwidth = 14.49794; #cm
tex.textheight = 23.69662; #cm

resetPar <- function() {
  dev.new()
  op <- par(no.readonly = TRUE)
  dev.off()
}

printf <- function(...) {
  cat(sprintf(...))
}

reload <- function(){
  rm(list = ls())
  source('globals.R')
  sapply(list.files(pattern="[.]R$", full.names=TRUE), source);
}