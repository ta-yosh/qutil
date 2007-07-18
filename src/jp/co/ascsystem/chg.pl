#!/usr/bin/perl

open(IN,"ls.lis");
@a=<IN>;
close(IN);
open(OU,">list.lis");
foreach $b (@a) {
     $c = $b;
     $d = $b;
     $c =~ s/(.+)/sed -f rep $1 > new\/$1/g;
     $d =~ s/Ikensyo/Qkan/g;
     print OU $c;                                       
     chomp($b);
     print OU "mv new/$b new/$d";
}
close(OU);
exit;

