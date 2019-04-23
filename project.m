clear;
p = dlmread('D:\ASU\SEM4\MC\Project\EEG_peak_dection\EEG_peak_dection\ekg_raw_16272.dat');
d = size(p);
hrate = [];
for i=1:128*60:d(1)
    rawECG = p(i:i+(128*60)-1,2);
    [peak_index, peak_values]= RPeakDetection(rawECG);
    size_index = size(peak_index);
    diff_index = p(peak_index(size_index(2))) - p(peak_index(1));
    hrate = [hrate (size_index(2)*60)/diff_index];
end
hrate_size = size(hrate);
label = [];
for i=1:1:hrate_size(2)
    if(hrate(i)<=60)
       label = [label 1];
    else label = [label 0];
    end
end

data = vertcat(hrate, label);
plot(hrate);

hr_std = [];
hr_mean = [];
hr_var = [];
k = 3;
for i=1:k:hrate_size(2)
    hr_mean = [hr_mean mean(hrate(:,i:i+(k-1)))];
    hr_std = [hr_std std(hrate(:,i:i+(k-1)))];
    hr_var = [hr_var var(hrate(:,i:i+(k-1)))];
end
    
features = vertcat(hr_mean, hr_std, hr_var);


