%% QRS detection Algorithm
% % clc
% % clear all
% % p = dlmread('250_Hz_data\MVA421\ekg_raw.dat');
% % RawECG = p(430000:432000,2);
function [Rpeak_index,Rpeak_values] = RPeakDetection(RawECG)
for i = 13:45
    index = i - 12;
    if(index < 3)
        LPFECG(index) = 0.5*(RawECG(i) - 2*RawECG(i - 6) + RawECG(i - 12));
    else
        LPFECG(index) = 0.5*(2*LPFECG(index - 1) - LPFECG(index - 2) + RawECG(i) - 2*RawECG(i - 6) + RawECG(i - 12));
    end
end

for i = 46:max(size(RawECG))
    index = i - 12;
    index2 = i - 45;
    LPFECG(index) = 0.5*(2*LPFECG(index - 1) - LPFECG(index - 2) + RawECG(i) - 2*RawECG(i - 6) + RawECG(i - 12));
    if(index2 < 2)
        HPFECG(index2) = (1/32)*(32*LPFECG(index - 16) + (LPFECG(index) - LPFECG(index - 32)));
    else
        HPFECG(index2) = (1/32)*(32*LPFECG(index - 16) - (HPFECG(index2 - 1) + LPFECG(index) - LPFECG(index - 32)));
    end
end

x = -HPFECG; %Filtered ECG
%x = RawECG;
BaseLine = mean(x);
% figure
% plot(x);hold on;
% plot(BaseLine*ones(1,length(x)),'r');
DynamicRangeUp = max(x) - BaseLine;
DynamicRangeDown = BaseLine - min(x);
thresholdUp = 0.002*DynamicRangeUp;
thresholdR = 0.5*DynamicRangeUp;
thresholdDown = 0.000002*DynamicRangeDown;
thresholdQ = 0.1*DynamicRangeDown;
% thresholdUp = 0.1*DynamicRangeUp;
% thresholdR = 0.35*DynamicRangeUp;
% thresholdDown = 0.01*DynamicRangeDown;
% thresholdQ = 0.05*DynamicRangeDown;
up = 1;
PreviousPeak = x(1);
k = 0;
maximum = -1000;
minimum = 1000;
possiblePeak = 0;
Rpeak = 0;
Rpeak_index = [];
Qpeak = 0;
Qpeak_index = [];
Speak = 0;
Speak_index = [];
PeakType = 0;
i = 1;
while (i < length(x))
    if(x(i) > maximum)
        maximum = x(i);

    end
    if(x(i) < minimum)
        minimum = x(i);

    end
    if(up == 1)
        if(x(i) < (maximum))
            if(possiblePeak == 0)
                possiblePeak = i;
            end
            if(x(i) < (maximum-thresholdUp))
                k = k + 1;
                peak_index(k) = possiblePeak-1;
                minimum = x(i);
                up = 0;
                possiblePeak = 0;
                if(PeakType == 0)                                  % if first peak then only consider the baseLine + threshold condition
                    if(x(peak_index(k))>BaseLine+thresholdR)

                        Rpeak = Rpeak + 1;
                        Rpeak_index = [Rpeak_index peak_index(k)];
                        PreviousPeak = x(peak_index(k));
                        %PeakType = 2;
                    end
                else
                    if(( abs((x(peak_index(k)) - PreviousPeak)/PreviousPeak) > 1.5) && (x(peak_index(k))>BaseLine+thresholdR))
                        Rpeak = Rpeak + 1;
                        Rpeak_index = [Rpeak_index peak_index(k)];
                        PreviousPeak = x(peak_index(k));
                        PeakType = 2;
                    end
                end
            end
            
            
        end
    else
        if(x(i) > minimum)
            if(possiblePeak == 0)
                possiblePeak = i;
            end
            if(x(i) > (minimum + thresholdDown))
                k = k + 1;
                peak_index(k) = possiblePeak-1;
                maximum = x(i);

                up = 1;
                possiblePeak = 0;
%                 if(abs((PreviousPeak - x(peak_index(k)))/x(peak_index(k))) > 0.5 && PeakType == 2 && (Rpeak_index(end) > (peak_index(k)-30)))
%                     Speak = Speak + 1;
%                     Speak_index = [Speak_index peak_index(k)];
%                     PreviousPeak = x(peak_index(k));
%                     PeakType = 3;
%                     i =  i + 120;
%                 elseif(PeakType == 3 && x(peak_index(k))< BaseLine - thresholdDown)
%                     Qpeak = Qpeak + 1;
%                     Qpeak_index = [Qpeak_index peak_index(k)];
%                     PreviousPeak = x(peak_index(k));
%                     PeakType = 1;
%                 elseif(PeakType == 1 && x(peak_index(k))< BaseLine - thresholdQ) %previous peak was labeled Q
%                     Qpeak_index = Qpeak_index(1:end-1);  %remove previous Q peak
%                     Qpeak_index = [Qpeak_index peak_index(k)];
%                     PreviousPeak = x(peak_index(k));
%                     PeakType = 1;
%                 elseif(x(peak_index(k))< BaseLine - thresholdQ)
%                     Qpeak = Qpeak + 1;
%                     Qpeak_index = [Qpeak_index peak_index(k)];
%                     PreviousPeak = x(peak_index(k));
%                     PeakType = 1;
%                 end
            end
        end
    end
    i = i + 1;
end
peak_values = x(peak_index);
% plot(x); hold on;
% scatter(peak_index, peak_values);

% Qpeak_index = Qpeak_index + 20;
% Rpeak_index = Rpeak_index + 23;
% Speak_index = Speak_index + 26;
if(length(Rpeak_index) > 0)
    Rpeak_values = RawECG(Rpeak_index);
else
    Rpeak_values = [];
end
if(length(Qpeak_index) > 0)
    Qpeak_values = RawECG(Qpeak_index);
else
    Qpeak_values = [];
end
if(length(Speak_index) > 0)
    Speak_values = RawECG(Speak_index);
else
    Speak_values = [];
end


