def not_empty(s):
    return s and s.strip()
def strip(s):
    return s.strip()

def main():
    s = """
    * 20201029	12345678	0	1	0.91
     * 20201029	12345678	0	2	0.07
     * 20201029	12345678	0	3	0.01
     * 20201029	12345678	0	4	0.005
     * 20201029	12345678	0	5	0.005
     * 20201029	12345678	1	1	0.95
     * 20201029	12345678	1	2	0.03
     * 20201029	12345678	1	3	0.01
     * 20201029	12345678	1	4	0.007
     * 20201029	12345678	1	5	0.003
     * 20201029	12345678	2	1	0
     * 20201029	12345678	2	2	0.9
     * 20201029	12345678	2	3	0.09
     * 20201029	12345678	2	4	0.008
     * 20201029	12345678	2	5	0.002
     * 20201029	12345678	3	1	0
     * 20201029	12345678	3	2	0
     * 20201029	12345678	3	3	0.96
     * 20201029	12345678	3	4	0.03
     * 20201029	12345678	3	5	0.01
     * 20201029	12345678	4	1	0
     * 20201029	12345678	4	2	0
     * 20201029	12345678	4	3	0
     * 20201029	12345678	4	4	0.97
     * 20201029	12345678	4	5	0.03
     * 20201029	12345678	5	1	0
     * 20201029	12345678	5	2	0
     * 20201029	12345678	5	3	0
     * 20201029	12345678	5	4	0
     * 20201029	12345678	5	5	1
     * 20201029	87654321	0	1	0.88
     * 20201029	87654321	0	2	0.1
     * 20201029	87654321	0	3	0.01
     * 20201029	87654321	0	4	0.005
     * 20201029	87654321	0	5	0.005
     * 20201029	87654321	1	1	0.92
     * 20201029	87654321	1	2	0.03
     * 20201029	87654321	1	3	0.01
     * 20201029	87654321	1	4	0.03
     * 20201029	87654321	1	5	0.01
     * 20201029	87654321	2	1	0
     * 20201029	87654321	2	2	0.85
     * 20201029	87654321	2	3	0.06
     * 20201029	87654321	2	4	0.05
     * 20201029	87654321	2	5	0.04
     * 20201029	87654321	3	1	0
     * 20201029	87654321	3	2	0
     * 20201029	87654321	3	3	0.94
     * 20201029	87654321	3	4	0.05
     * 20201029	87654321	3	5	0.01
     * 20201029	87654321	4	1	0
     * 20201029	87654321	4	2	0
     * 20201029	87654321	4	3	0
     * 20201029	87654321	4	4	0.9
     * 20201029	87654321	4	5	0.1
     * 20201029	87654321	5	1	0
     * 20201029	87654321	5	2	0
     * 20201029	87654321	5	3	0
     * 20201029	87654321	5	4	0
     * 20201029	87654321	5	5	1"""
    d = list(filter(not_empty, s.split("*")))
    d = list(map(strip, d))
    for data in d:
        dl = data.split('\t')
        print('testData.add(new RaceIntervalInfo("{}", "{}", {}, {}, {}));'.format(dl[0], dl[1], dl[2], dl[3], dl[4]))


if __name__ == '__main__':
    main()