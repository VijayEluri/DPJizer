package readwritedisjointness;

class C {

	int a[], b[], c[];
	D d[];

	void m() {
		foreach (int i in 0, a.length) {
			if (a[0] + b[0] + d[0].f + d[0].g == 0) {
				c[i] = 0;
			}
		}
		a = b;
	}

}

class D<region P> {
	int f in P;
	int g in Q;
}