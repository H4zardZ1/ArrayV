package sorts.hybrid;

import sorts.templates.MultiWayMergeSorting;
import main.ArrayVisualizer;

/*
 * 
MIT License

Copyright (c) 2021 aphitorite

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 *
 */

final public class RemiSort extends MultiWayMergeSorting {
    public RemiSort(ArrayVisualizer arrayVisualizer) {
        super(arrayVisualizer);
        
        this.setSortListName("Remi");
        this.setRunAllSortsName("Remi Sort");
        this.setRunSortName("Remisort");
        this.setCategory("Hybrid Sorts");
        this.setComparisonBased(true);
        this.setBucketSort(false);
        this.setRadixSort(false);
        this.setUnreasonablySlow(false);
        this.setUnreasonableLimit(0);
        this.setBogoSort(false);
    }
	
	//stable sorting algorithm that guarantees worst case performance of
	//O(n log n) comparisons and O(n) moves in O(n^2/3) memory
	
	private int ceilCbrt(int n) {
		int a = 0, b = Math.min(1291, n);
		
		while(a < b) {
			int m = (a+b)/2;
			
			if(m*m*m >= n) b = m;
			else           a = m+1;
		}
		
		return a;
	}
	
	private void siftDown(int[] array, int[] keys, int r, int len, int a, int t) {
		int j = r;
		
		while(2*j + 1 < len) {
			j = 2*j + 1;
			
			if(j+1 < len) {
				int cmp = Reads.compareIndices(array, a+keys[j+1], a+keys[j], 0.2, true);
				
				if(cmp > 0 || (cmp == 0 && Reads.compareOriginalValues(keys[j+1], keys[j]) > 0)) j++;
			}
		}
		for(int cmp = Reads.compareIndices(array, a+t, a+keys[j], 0.2, true);
		
			cmp > 0 || (cmp == 0 && Reads.compareOriginalValues(t, keys[j]) > 0);
			
			j = (j-1)/2,
			cmp = Reads.compareIndices(array, a+t, a+keys[j], 0.2, true));
		
		for(int t2; j > r; j = (j-1)/2) {
			t2 = keys[j];
			Highlights.markArray(3, j);
			Writes.write(keys, j, t, 0.2, false, true);
			t = t2;
		}
		Highlights.markArray(3, r);
		Writes.write(keys, r, t, 0.2, false, true);
	}
	
	private void tableSort(int[] array, int[] keys, int a, int b) {
		int len = b-a;
		
		for(int i = (len-1)/2; i >= 0; i--)
			this.siftDown(array, keys, i, len, a, keys[i]);
		
		for(int i = len-1; i > 0; i--) {
			int t = keys[i];
			Highlights.markArray(3, i);
			Writes.write(keys, i, keys[0], 1, false, true);
			this.siftDown(array, keys, 0, i, a, t);
		}
		Highlights.clearMark(3);
		
		for(int i = 0; i < len; i++) {
			Highlights.markArray(2, i);
			if(Reads.compareOriginalValues(i, keys[i]) != 0) {
				int t = array[a+i];
				int j = i, next = keys[i];
				
				do {
					Writes.write(array, a+j, array[a+next], 1, true, false);
					Writes.write(keys, j, j, 1, true, true);
					
					j = next;
					next = keys[next];
				}
				while(Reads.compareOriginalValues(next, i) != 0);
				
				Writes.write(array, a+j, t, 1, true, false);
				Writes.write(keys, j, j, 1, true, true);
			}
		}
		Highlights.clearMark(2);
	}
	
	private void kWayMerge(int[] array, int[] keys, int[] heap, int a, int a1, int b, int[] p, int[] pa, int bLen, int rLen) {
		int k = p.length;
		if(k < 2) return;
		
		for(int i = 0; i < k; i++)
			Writes.write(heap, i, i, 0, false, true);

		for(int i = (k-1)/2; i >= 0; i--)
			this.siftDown(array, heap, pa, heap[i], i, k);
		
		int tVal = bLen-1, size = k;
		
		do {
			int c = 0;
			while(pa[c] - (a + p[c]*bLen) < bLen) c++;
			
			for(int n = 0; n < bLen; n++) {
				int min = heap[0];
				
				Writes.write(array, a + p[c]*bLen + n, array[pa[min]], 0, true, false);
				Writes.write(pa, min, pa[min]+1, 1, false, true);

				if(pa[min] == Math.min(a1 + min*rLen, b))
					this.siftDown(array, heap, pa, heap[--size], 0, size);
				else 
					this.siftDown(array, heap, pa, heap[0], 0, size);
			}
			Writes.write(keys, tVal++, p[c]-1, 0, false, true);
			Writes.write(p, c, p[c]+1, 1, true, true);
		}
		while(size > 0);
		
		tVal = 0;
		
		for(int i = 0; i < k; i++) {
			while(a + p[i]*bLen < pa[i]) {
				Writes.write(keys, tVal++, p[i]-1, 0, false, true);
				Writes.write(p, i, p[i]+1, 1, true, true);
			}
		}
		Writes.arraycopy(array, a, array, b-bLen, bLen, 1, true, false);
	}
	
	private void blockCycle(int[] array, int[] keys, int a, int bLen, int bCnt) {
		int p = a;
		a += bLen;
		
		for(int i = 0; i < bCnt; i++) {
			if(Reads.compareOriginalValues(i, keys[i]) != 0) {
				Writes.arraycopy(array, a + i*bLen, array, p, bLen, 1, true, false);
				int j = i, next = keys[i];
				
				do {
					if(j >= bLen-1)
						Writes.arraycopy(array, a + next*bLen, array, a + j*bLen, bLen, 1, true, false);
					Writes.write(keys, j, j, 1, true, true);
					
					j = next;
					next = keys[next];
				}
				while(Reads.compareOriginalValues(next, i) != 0);
				
				Writes.arraycopy(array, p, array, a + j*bLen, bLen, 1, true, false);
				Writes.write(keys, j, j, 1, true, true);
			}
		}
	}
    
    @Override
    public void runSort(int[] array, int length, int bucketCount) {
		int bLen = this.ceilCbrt(length);
		int rLen = bLen*bLen;
		
		int a = length%bLen, b = length;
		length -= a;
		
		int bCnt = length/bLen - 1;
		int rCnt = (length-1)/rLen;
		
		int[] keys = Writes.createExternalArray(rLen+a);
		int[] buf  = Writes.createExternalArray(rLen+a);
		
		int[] heap = new int[rCnt];
		int[] p    = new int[rCnt];
		int[] pa   = new int[rCnt];
		
		int alloc = 3*rCnt;
		Writes.changeAllocAmount(alloc);
		
		for(int i = 0; i < keys.length; i++)
			Writes.write(keys, i, i, 1, true, true);
		
		int i = a+rLen;
		this.tableSort(array, keys, 0, i);
		Writes.arraycopy(array, 0, buf, 0, buf.length, 1, true, true);
		
		for(int j = 0; i < b; i += rLen, j++) {
			int e = Math.min(i+rLen, b);
			this.tableSort(array, keys, i, e);
			
			Writes.write(pa, j, i, 0, false, true);
		}
		
		if(rCnt > 0) {
			for(int j = 1; j < rCnt; j++)
				Writes.write(p, j, (j+1)*bLen, 0, false, true);
			
			this.kWayMerge(array, keys, heap, a, a+2*rLen, b, p, pa, bLen, rLen);
			if(rCnt > 1) this.blockCycle(array, keys, a, bLen, bCnt);
			
			i = 0;
			Highlights.markArray(2, i);
			int j = a+rLen, k = 0;
			
			while(i < buf.length && j < b) {
				if(Reads.compareValues(buf[i], array[j]) <= 0) {
					Highlights.markArray(2, i);
					Writes.write(array, k++, buf[i++], 1, true, false);
				}
				else Writes.write(array, k++, array[j++], 1, true, false);
			}
			while(i < buf.length) {
				Highlights.markArray(2, i);
				Writes.write(array, k++, buf[i++], 1, true, false);
			}
		}
		Writes.deleteExternalArray(keys);
		Writes.deleteExternalArray(buf);
		Writes.changeAllocAmount(-alloc);
    }
}